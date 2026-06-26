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

    // ... los 3 métodos van acá (pasos 5, 6 y 7)
}
