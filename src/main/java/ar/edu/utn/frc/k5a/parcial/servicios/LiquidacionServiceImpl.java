package ar.edu.utn.frc.k5a.parcial.servicios;

import ar.edu.utn.frc.k5a.parcial.dto.LiquidacionDTO;
import ar.edu.utn.frc.k5a.parcial.excepciones.TarjetaInexistenteException;
import ar.edu.utn.frc.k5a.parcial.modelo.Consumo;
import ar.edu.utn.frc.k5a.parcial.modelo.ItemLiquidacion;
import ar.edu.utn.frc.k5a.parcial.modelo.Liquidacion;
import ar.edu.utn.frc.k5a.parcial.modelo.Tarjeta;
import ar.edu.utn.frc.k5a.parcial.repositorios.ConsumoRepository;
import ar.edu.utn.frc.k5a.parcial.repositorios.CotizacionRepository;
import ar.edu.utn.frc.k5a.parcial.repositorios.LiquidacionRepository;
import ar.edu.utn.frc.k5a.parcial.repositorios.TarjetaRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LiquidacionServiceImpl implements LiquidacionService {
    // CONSTANTES DE LA CONSIGNA FACIL DE MODIFICAR
    private static final double IVA_ARS = 0.21;
    private static final double ADICION_OTROS = 0.12;
    private static final double IMPUESTO_EXTRANJERA = 0.075;
    private static final double PORC_DESC_COMBUSTIBLE = 0.15;
    private static final double TOPE_DESC_COMBUSTIBLE = 750.0;
    private static final double PORC_DESC_SUPERMERCADO = 0.20;
    private static final double TOPE_DESC_SUPERMERCADO = 3000.0;
    private static final double PORC_DESC_RESTAURANTE = 0.25;
    private static final int DIA_MIN_REST = 10;
    private static final int DIA_MAX_REST = 15;

    private final CotizacionRepository cotizacionRepository;
    private final ConsumoRepository consumoRepository;
    private final TarjetaRepository tarjetaRepository;
    private final LiquidacionRepository liquidacionRepository;
    private final Map<String, Double> cotizaciones;

    public LiquidacionServiceImpl(CotizacionRepository cotizacionRepository,
                                  ConsumoRepository consumoRepository,
                                  TarjetaRepository tarjetaRepository,
                                  LiquidacionRepository liquidacionRepository) {
        this.cotizacionRepository = cotizacionRepository;
        this.consumoRepository = consumoRepository;
        this.tarjetaRepository = tarjetaRepository;
        this.liquidacionRepository = liquidacionRepository;
        this.cotizaciones = cotizacionRepository.obtenerCotizaciones();
    }

    @Override
    public LiquidacionDTO generarLiquidacion(long idTarjeta, int anio, int mes) throws TarjetaInexistenteException {
        Optional<Tarjeta> opTarjeta = tarjetaRepository.buscarPorId(idTarjeta);
        if (opTarjeta.isEmpty()) throw new TarjetaInexistenteException(idTarjeta);
        Tarjeta tarjeta = opTarjeta.get();

        //CONTROL DE IDEMPOTENCIA
        Optional<Liquidacion> optionalLiquidacionExistente = liquidacionRepository.buscarPorTarjetaYPeriodo(tarjeta.getNumero(), anio, mes);
        if (optionalLiquidacionExistente.isPresent()){
            return map(optionalLiquidacionExistente.get());
        }

        ItemLiquidacion total = new ItemLiquidacion();
        List<Consumo> consumos = consumoRepository.buscarPorTarjetaYPeriodo(tarjeta.getNumero(), anio, mes);
        for (Consumo c : consumos) {
            total = total.acumular(this.calcularConsumo(c));
        }

        Liquidacion l = new Liquidacion(tarjeta, anio, mes);
        l.setTotalConsumos(total.getConsumos());
        l.setTotalImpuestos(total.getImpuestos());
        l.setTotalDescuentos(total.getDescuentos());
        l.setTotalAPagar(total.getTotal());
        // Cambio clave: Forzamos explícitamente Consumos + Impuestos - Descuentos
        l.setTotalAPagar(total.getConsumos() + total.getImpuestos() - total.getDescuentos());
        liquidacionRepository.guardar(l);

        return map(l);
    }

    private LiquidacionDTO map(Liquidacion l) {
        LiquidacionDTO dto = new LiquidacionDTO();
        dto.setId(l.getId());
        dto.setAnio(l.getAnio());
        dto.setMes(l.getMes());
        dto.setNumeroTarjeta(l.getTarjeta().getNumero());
        dto.setTitular(l.getTarjeta().getTitular());
        dto.setTotalAPagar(l.getTotalAPagar());
        dto.setTotalConsumos(l.getTotalConsumos());
        dto.setTotalImpuestos(l.getTotalImpuestos());
        dto.setTotalDescuentos(l.getTotalDescuentos());
        return dto;
    }

    private ItemLiquidacion calcularConsumo(Consumo c) {
        double consumo = c.getMonto();
        double impuesto = 0;
        double descuento = 0;

        if (c.getMoneda().equals("ARS")) {
            // IVA: 21% sobre todos los consumos en pesos (sobre el monto completo)
            impuesto = consumo * IVA_ARS;
            switch (c.getRubro()) {
                case "COMBUSTIBLE": {
                    descuento = Math.min(consumo * PORC_DESC_COMBUSTIBLE, TOPE_DESC_COMBUSTIBLE);
                    break;
                }
                case "SUPERMERCADO": {
                    descuento = Math.min(consumo * PORC_DESC_SUPERMERCADO, TOPE_DESC_SUPERMERCADO);
                    break;
                }
                case "RESTAURANTES": {
                    if (c.getDia() >= DIA_MIN_REST && c.getDia() <= DIA_MAX_REST) {
                        descuento = consumo * PORC_DESC_RESTAURANTE;
                    }
                    break;
                }
                case "OTROS": {
                    // Resolucion BDA 1234/95: 12% adicional para rubro OTROS
                    impuesto += consumo * ADICION_OTROS;
                    break;
                }
            }
        } else {
            // Protección contra NullPointerException si la moneda no está en el Map
            double cotizacion = this.cotizaciones.getOrDefault(c.getMoneda(), 1.0);
            // Moneda extranjera: se convierte a ARS y paga 7.5% (impuesto extraordinario)
            consumo *= cotizacion;
            impuesto = consumo * IMPUESTO_EXTRANJERA;
        }

        return new ItemLiquidacion(consumo, impuesto, descuento);
    }

    @Override
    public List<String> getLiquidacionesPendientes(int anio, int mes) {
        return tarjetaRepository.buscarSinLiquidacion(anio, mes)
                .stream()
                .map(Tarjeta::getNumero)
                .collect(Collectors.toList());
    }
    @Override
    public LiquidacionDTO buscarLiquidacionExistente(String numeroTarjeta, int anio, int mes){
        Optional<Liquidacion> optionalLiquidacion = liquidacionRepository.buscarPorTarjetaYPeriodo(numeroTarjeta, anio, mes);
        if(optionalLiquidacion.isEmpty()) {
            return null;
        }
        return map(optionalLiquidacion.get());
        }
    }
    @Override
    public List<LiquidacionDTO> liquidarLote(String rutaArchivo) throws IOException {
        // Corrección para rutas en entornos Windows si vienen de un URL resource
        if (rutaArchivo.matches("/[A-Za-z]:.*")) {
            rutaArchivo = rutaArchivo.substring(1);
        }

        return Files.lines(Paths.get(rutaArchivo))
                .map(String::trim)
                .filter(linea -> !linea.isEmpty())
                .map(linea -> linea.split(";"))
                .map(campos -> {
                    try {
                        long idTarjeta = Long.parseLong(campos[0].trim());
                        int anio = Integer.parseInt(campos[1].trim());
                        int mes = Integer.parseInt(campos[2].trim());

                        // Llamamos al método que ya hace el trabajo pesado y guarda en la BD
                        return this.generarLiquidacion(idTarjeta, anio, mes);
                    } catch (TarjetaInexistenteException e) {
                        // Envolvemos la checked exception en una RuntimeException para que la lambda compile
                        throw new RuntimeException("Error en lote: Tarjeta inexistente ID " + campos[0], e);
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                        throw new RuntimeException("Error en el formato de la línea del CSV", e);
                    }
                })
                .collect(Collectors.toList());
    }
}
