package ar.edu.utn.frc.k5a.parcial.servicios;

import ar.edu.utn.frc.k5a.parcial.dto.LiquidacionDTO;
import ar.edu.utn.frc.k5a.parcial.excepciones.TarjetaInexistenteException;
import ar.edu.utn.frc.k5a.parcial.repositorios.CotizacionRepository;
import ar.edu.utn.frc.k5a.parcial.repositorios.ConsumoRepository;
import ar.edu.utn.frc.k5a.parcial.repositorios.TarjetaRepository;
import ar.edu.utn.frc.k5a.parcial.repositorios.LiquidacionRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LiquidacionServiceImplTest {

    private EntityManager em;
    private EntityManagerFactory emf;
    private LiquidacionService liquidacionService;

    @BeforeEach
    void setUp() {
        emf = Persistence.createEntityManagerFactory("LiquidacionesPU");
        em = emf.createEntityManager();

        CotizacionRepository cotizacionRepository = new CotizacionRepository(em);
        ConsumoRepository consumoRepository = new ConsumoRepository(em);
        TarjetaRepository tarjetaRepository = new TarjetaRepository(em);
        LiquidacionRepository liquidacionRepository = new LiquidacionRepository(em);

        liquidacionService = new LiquidacionServiceImpl(
                cotizacionRepository,
                consumoRepository,
                tarjetaRepository,
                liquidacionRepository
        );
    }

    @AfterEach
    void tearDown() {
        if (em != null && em.isOpen()) {
            em.close();
        }
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    void testTarjetaInexistente() {
        assertThrows(TarjetaInexistenteException.class, () -> liquidacionService.generarLiquidacion(999999, 2, 2));
    }

    @Test
    void testGenerarLiquidacion() {
        em.getTransaction().begin();
        LiquidacionDTO liquidacion = liquidacionService.generarLiquidacion(1L, 2026, 5);
        em.getTransaction().commit();

        assertNotNull(liquidacion);
        assertEquals("4500123412340001", liquidacion.getNumeroTarjeta());
        assertEquals(2026, liquidacion.getAnio());
        assertEquals(5, liquidacion.getMes());
        assertEquals("Juan Perez", liquidacion.getTitular());

        assertEquals(436400.0, liquidacion.getTotalConsumos(), 0.01);
        assertEquals(0.45, liquidacion.getTotalImpuestos(), 0.01);
        assertEquals(4950.0, liquidacion.getTotalDescuentos(), 0.01);
        assertEquals(431450.45, liquidacion.getTotalAPagar(), 0.01);
    }

    @Test
    void testGetLiquidacionesPendientes() {
        em.getTransaction().begin();
        List<String> pendientesAntes = liquidacionService.getLiquidacionesPendientes(2026, 5);
        assertEquals(10, pendientesAntes.size());

        liquidacionService.generarLiquidacion(1L, 2026, 5);
        em.getTransaction().commit();

        em.getTransaction().begin();
        List<String> pendientesDespues = liquidacionService.getLiquidacionesPendientes(2026, 5);
        assertEquals(9, pendientesDespues.size());
        assertFalse(pendientesDespues.contains("4500123412340001"));
        em.getTransaction().commit();
    }

    @Test
    void testLiquidarLote() throws IOException {
        URL url = getClass().getClassLoader().getResource("liquidaciones.csv");
        assertNotNull(url, "No se encontró el archivo de lotes liquidaciones.csv");

        em.getTransaction().begin();
        List<LiquidacionDTO> liquidaciones = liquidacionService.liquidarLote(url.getPath());
        em.getTransaction().commit();

        assertFalse(liquidaciones.isEmpty());
    }
}