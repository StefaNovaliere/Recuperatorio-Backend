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
        double consumo = c.getMonto();
        double impuesto = 0;
        double descuento = 0;

        if (c.getMoneda().equals("ARS")) {
            switch (c.getRubro()) {
                case "COMBUSTIBLE": {
                    descuento = Math.min(consumo * 0.15, 750);
                    break;
                }
                case "SUPERMERCADO": {
                    descuento = Math.min(consumo * 0.20, 3000);
                    break;
                }
                case "RESTAURANTES": {
                    if (c.getDia() >= 10 && c.getDia() <= 15) {
                        descuento = consumo * 0.25;
                    }
                    break;
                }
                case "OTROS": {
                    impuesto = consumo * 0.0075;
                    break;
                }
            }
        } else {
            // Solo el rubro OTROS paga impuesto, sobre el monto ORIGINAL (sin convertir)
            if (c.getRubro().equals("OTROS")) {
                impuesto = consumo * 0.0075;
            }
            // El consumo se convierte a ARS para el total de consumos
            consumo *= this.cotizaciones.get(c.getMoneda());
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
                .map(l -> l.split(";"))
                .map(p -> this.generarLiquidacion(
                        Long.parseLong(p[0].trim()),
                        Integer.parseInt(p[1].trim()),
                        Integer.parseInt(p[2].trim())
                ))
                .collect(Collectors.toList());
    }
}