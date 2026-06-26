package ar.edu.utn.frc.k5a.parcial.servicios;

import ar.edu.utn.frc.k5a.parcial.dto.LiquidacionDTO;
import ar.edu.utn.frc.k5a.parcial.excepciones.TarjetaInexistenteException;
import ar.edu.utn.frc.k5a.parcial.modelo.*;
import ar.edu.utn.frc.k5a.parcial.repositorios.*;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
public class LiquidacionServiceImpl implements LiquidacionService {

    private CotizacionRepository cotizacionRepo;
    private ConsumoRepository consumoRepo;
    private TarjetaRepository tarjetaRepo;
    private LiquidacionRepository liquidacionRepo;

    @Override
    public LiquidacionDTO generarLiquidacion(long idTarjeta, int anio, int mes)
            throws TarjetaInexistenteException {

        // 1. Validar que la tarjeta exista
        Tarjeta tarjeta = tarjetaRepo.findById(idTarjeta);
        if (tarjeta == null) {
            throw new TarjetaInexistenteException(idTarjeta);
        }

        // 2. Obtener consumos del período
        List<Consumo> consumos = consumoRepo.findByTarjetaYPeriodo(idTarjeta, anio, mes);

        // 3. Procesar cada consumo y acumular totales
        ItemLiquidacion total = new ItemLiquidacion();
        for (Consumo consumo : consumos) {
            ItemLiquidacion item = procesarConsumo(consumo);
            total = total.acumular(item);
        }

        // 4. Persistir la liquidación en la BD
        Liquidacion liquidacion = new Liquidacion();
        liquidacion.setTarjeta(tarjeta);
        liquidacion.setAnio(anio);
        liquidacion.setMes(mes);
        liquidacion.setTotalConsumos(total.getConsumos());
        liquidacion.setTotalImpuestos(total.getImpuestos());
        liquidacion.setTotalDescuentos(total.getDescuentos());
        liquidacion.setTotalAPagar(total.getTotal());

        liquidacionRepo.guardar(liquidacion);

        // 5. Armar y devolver el DTO
        return armarDTO(liquidacion, tarjeta);
    }

    @Override
    public List<String> getLiquidacionesPendientes(int anio, int mes) {
        List<Tarjeta> tarjetas = tarjetaRepo.findSinLiquidacion(anio, mes);
        return tarjetas.stream()
                .map(Tarjeta::getNumero)
                .collect(Collectors.toList());
    }

    @Override
    public List<LiquidacionDTO> liquidarLote(String rutaArchivo) throws IOException {
        List<LiquidacionDTO> resultado = new ArrayList<>();

        List<String> lineas = Files.readAllLines(Paths.get(rutaArchivo));
        for (String linea : lineas) {
            String[] partes = linea.split(";");
            long idTarjeta = Long.parseLong(partes[0].trim());
            int anio = Integer.parseInt(partes[1].trim());
            int mes = Integer.parseInt(partes[2].trim());

            LiquidacionDTO dto = generarLiquidacion(idTarjeta, anio, mes);
            resultado.add(dto);
        }

        return resultado;
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private ItemLiquidacion procesarConsumo(Consumo consumo) {
        double montoOriginal = consumo.getMonto();
        String moneda = consumo.getMoneda();
        String rubro = consumo.getRubro();
        int dia = consumo.getDia();

        // Convertir monto a ARS
        Cotizacion cotizacion = cotizacionRepo.findByMoneda(moneda);
        double montoEnARS = montoOriginal * cotizacion.getTasaCambio();

        // Descuento (solo moneda ARS)
        double descuento = 0;
        if ("ARS".equals(moneda)) {
            descuento = calcularDescuento(montoOriginal, rubro, dia);
        }

        // Impuesto (sobre monto completo, antes de descuentos)
        double impuesto = calcularImpuesto(montoOriginal, moneda, rubro);

        return new ItemLiquidacion(montoEnARS, impuesto, descuento);
    }

    private double calcularDescuento(double monto, String rubro, int dia) {
        return switch (rubro) {
            case "COMBUSTIBLE"  -> Math.min(monto * 0.15, 750);
            case "SUPERMERCADO" -> Math.min(monto * 0.20, 3000);
            case "RESTAURANTES" -> (dia >= 10 && dia <= 15) ? monto * 0.25 : 0;
            default -> 0;
        };
    }

    private double calcularImpuesto(double montoOriginal, String moneda, String rubro) {
        if ("ARS".equals(moneda)) {
            // IVA ya incluido en el precio, solo Resolución BDA es adicional
            if ("OTROS".equals(rubro)) {
                return montoOriginal * 0.12;
            }
            return 0;
        } else {
            // Impuesto extraordinario 7.5% sobre monto en moneda original
            return montoOriginal * 0.075;
        }
    }

    private LiquidacionDTO armarDTO(Liquidacion liquidacion, Tarjeta tarjeta) {
        LiquidacionDTO dto = new LiquidacionDTO();
        dto.setId(liquidacion.getId());
        dto.setNumeroTarjeta(tarjeta.getNumero());
        dto.setTitular(tarjeta.getTitular());
        dto.setAnio(liquidacion.getAnio());
        dto.setMes(liquidacion.getMes());
        dto.setTotalConsumos(liquidacion.getTotalConsumos());
        dto.setTotalImpuestos(liquidacion.getTotalImpuestos());
        dto.setTotalDescuentos(liquidacion.getTotalDescuentos());
        dto.setTotalAPagar(liquidacion.getTotalAPagar());
        return dto;
    }
}
