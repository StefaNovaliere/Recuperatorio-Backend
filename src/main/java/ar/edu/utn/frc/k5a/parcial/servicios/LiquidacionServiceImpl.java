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

    // CONSTANTES DE LAS REGLAS (faciles de modificar si la consigna cambia un porcentaje)
    private static final double IVA_ARS = 0.21;
    private static final double ADICION_OTROS = 0.10;
    private static final double IMPUESTO_EXTRANJERA = 0.08;
    private static final double PORC_DESC_COMBUSTIBLE = 0.10;
    private static final double TOPE_DESC_COMBUSTIBLE = 1000.0;
    private static final double PORC_DESC_SUPERMERCADO = 0.20;
    private static final double TOPE_DESC_SUPERMERCADO = 3000.0;
    private static final double PORC_DESC_RESTAURANTE = 0.20;
    private static final double PORC_DESC_INDUMENTARIA = 0.05;
    private static final double TOPE_DESC_INDUMENTARIA = 500.0;
    private static final int DIA_MIN_REST = 1;
    private static final int DIA_MAX_REST = 10;

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
        // TODO (presencial): implementar
        //Primero busco en la base de datos si la tarjeta ingresada existe
        Optional<Tarjeta> opTarjeta = tarjetaRepository.buscarPorId(idTarjeta);
        //Si no existe...
        if(opTarjeta.isEmpty()) throw new TarjetaInexistenteException(idTarjeta);
        //Si existe
        Tarjeta tarjeta = opTarjeta.get();
        //Busco si ya existe una liquidación para esta tarjeta en el mismo mes/año
        Optional<Liquidacion> exist = liquidacionRepository.buscarPorTarjetaYPeriodo(tarjeta.getNumero(), anio, mes);
        if (exist.isPresent()){
            return map(exist.get());
        }
        //Traigo la lista de todo lo que gastó
        List<Consumo> consumos = consumoRepository.buscarPorTarjetaYPeriodo(tarjeta.getNumero(), anio, mes);
        //Sumar y calcular
        ItemLiquidacion total = new ItemLiquidacion();
        for (Consumo c: consumos){
            total = total.acumular(this.calcularConsumo(c));
        }
        //Creo la entidad con los datos básicos
        Liquidacion l = new Liquidacion(tarjeta, anio, mes);
        l.setTotalConsumos(total.getConsumos());
        l.setTotalImpuestos(total.getImpuestos());
        l.setTotalDescuentos(total.getDescuentos());
        //Calculo el neto final
        l.setTotalAPagar(total.getConsumos() + total.getImpuestos() - total.getDescuentos());
        //Persistir y entregar
        liquidacionRepository.guardar(l);
        return map(l);
    }
    @Override
    public List<String> tarjetasConConsumoEnMoneda(String moneda, int anio, int mes){
        List<Consumo> consumosMoneda = consumoRepository.buscarPorMonedaYFecha(moneda, anio, mes);
        return consumosMoneda.stream()
                .map(consumo -> consumo.getTarjeta().getNumero())
                .distinct()
                .collect(Collectors.toList());
    }
    @Override
    public List<String> tarjetaSinLiquidacion(int anio, int mes) {
        List<Tarjeta> tarjetasPendientes = tarjetaRepository.buscarSinLiquidacion(anio, mes);
        return tarjetasPendientes.stream()
                .map(tarjeta -> tarjeta.getNumero())
                .collect(Collectors.toList());
        // TODO (presencial): implementar
    }
    @Override
    public LiquidacionDTO buscarLiquidacionExistente(String numeroTarjeta, int anio, int mes){
        Optional<Liquidacion> liquidacionExistente = liquidacionRepository.buscarPorTarjetaYPeriodo(numeroTarjeta, anio, mes);
        if (liquidacionExistente.isEmpty()){
            return null;
        }
        return map(liquidacionExistente.get());
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
    private ItemLiquidacion calcularConsumo(Consumo c) {
        double consumo = c.getMonto();
        double impuesto = 0;
        double descuento = 0;

        if (c.getMoneda().equals("ARS")) {
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
                case "INDUMENTARIA": {
                    descuento = Math.min(consumo *  PORC_DESC_INDUMENTARIA, TOPE_DESC_INDUMENTARIA);
                    break;
                }
                case "OTROS": {
                    impuesto += consumo * ADICION_OTROS;
                    break;
                }
            }
        } else {
            double cotizacion = this.cotizaciones.getOrDefault(c.getMoneda(), 1.0);
            consumo *= cotizacion;
            impuesto = consumo * IMPUESTO_EXTRANJERA;
        }

        return new ItemLiquidacion(consumo, impuesto, descuento);
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
}
