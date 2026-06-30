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
        // TODO (presencial): implementar
        throw new UnsupportedOperationException("TODO: generarLiquidacion");
    }

    @Override
    public List<String> getLiquidacionesPendientes(int anio, int mes) {
        // TODO (presencial): implementar
        throw new UnsupportedOperationException("TODO: getLiquidacionesPendientes");
    }

    @Override
    public List<LiquidacionDTO> liquidarLote(String rutaArchivo) throws IOException {
        // TODO (presencial): implementar
        throw new UnsupportedOperationException("TODO: liquidarLote");
    }
}
