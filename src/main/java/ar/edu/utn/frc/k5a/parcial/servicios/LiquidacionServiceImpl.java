package ar.edu.utn.frc.k5a.parcial.servicios;

import ar.edu.utn.frc.k5a.parcial.dto.LiquidacionDTO;
import ar.edu.utn.frc.k5a.parcial.excepciones.LimiteExcedidoException;
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
import java.util.stream.Collectors;

public class LiquidacionServiceImpl implements LiquidacionService {

    private final CotizacionRepository cotizacionRepository;
    private final ConsumoRepository consumoRepository;
    private final TarjetaRepository tarjetaRepository;
    private final LiquidacionRepository liquidacionRepository;
    private final Map<String, Double> cotizaciones;

    public LiquidacionServiceImpl(
            CotizacionRepository cotizacionRepository,
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
    public LiquidacionDTO generarLiquidacion(long idTarjeta, int anio, int mes)
            throws TarjetaInexistenteException {
        Tarjeta tarjeta = tarjetaRepository.buscarPorId(idTarjeta)
                .orElseThrow(() -> new TarjetaInexistenteException(idTarjeta));

        ItemLiquidacion total = new ItemLiquidacion();
        List<Consumo> consumos = consumoRepository.buscarPorTarjetaYPeriodo(tarjeta.getNumero(), anio, mes);
        for (Consumo c : consumos) {
            total = total.acumular(this.calcularConsumo(c));
        }
        if (total.getConsumos() > tarjeta.getLimiteCredito()) {
            throw new LimiteExcedidoException(
                    "La tarjeta " + tarjeta.getNumero() + " supero su limite de credito");
        }
        Liquidacion l = new Liquidacion();
        l.setTarjeta(tarjeta);
        l.setAnio(anio);
        l.setMes(mes);
        l.setTotalConsumos(total.getConsumos());
        l.setTotalImpuestos(total.getImpuestos());
        l.setTotalDescuentos(total.getDescuentos());
        l.setTotalAPagar(total.getTotal());
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
        // Todos los importes se calculan sobre el monto convertido a ARS
        double consumo = c.getMonto() * this.cotizaciones.get(c.getMoneda());
        double impuesto = 0;
        double descuento = 0;

        if (c.getMoneda().equals("ARS")) {
            // IVA: 21% sobre todos los consumos en pesos
            impuesto = consumo * 0.21;

            switch (c.getRubro()) {
                case "OTROS": {
                    // Impuesto adicional del 12% para el rubro OTROS
                    impuesto += consumo * 0.12;
                    break;
                }
                case "EDUCACION":{
                    impuesto = 0;
                    break;
                }
                case "SALUD": {
                    if (c.getDia() >= 8) {
                        descuento = Math.min(consumo * 0.10, 500);
                    }
                    break;
                }
                case "SUPERMERCADO": {
                    descuento = Math.min(consumo * 0.20, 3000);
                    break;
                }
                case "COMBUSTIBLE": {
                    descuento = Math.min(consumo * 0.15, 750);
                    break;
                }
                case "RESTAURANTES": {
                    if (c.getDia() >= 10 && c.getDia() <= 15) {
                        descuento = consumo * 0.25;
                    }
                    break;
                }
            }
        } else {
            // Consumos en moneda extranjera: impuesto del 7.5% sobre el monto convertido
            impuesto = consumo * 0.075;
        }

        return new ItemLiquidacion(consumo, impuesto, descuento);
    }

    @Override
    public List<String> getLiquidacionesPendientes(int anio, int mes) {
        return tarjetaRepository.buscarSinLiquidacion(anio, mes)
                .stream()
                .map(t -> t.getNumero())
                .collect(Collectors.toList());
    }

    @Override
    public List<LiquidacionDTO> liquidarLote(String rutaArchivo) throws IOException {
        // En Windows, url.getPath() devuelve "/C:/..." (barra inicial invalida para Paths.get)
        if (rutaArchivo.matches("/[A-Za-z]:.*")) {
            rutaArchivo = rutaArchivo.substring(1);
        }
        return Files.lines(Paths.get(rutaArchivo))
                .map(String::trim)
                .filter(linea -> !linea.isEmpty())           // saltea lineas en blanco
                .map(linea -> linea.split(";"))
                .map(campos -> this.generarLiquidacion(
                        Long.parseLong(campos[0].trim()),    // idTarjeta
                        Integer.parseInt(campos[1].trim()),  // anio
                        Integer.parseInt(campos[2].trim())   // mes
                ))
                .collect(Collectors.toList());
    }
}