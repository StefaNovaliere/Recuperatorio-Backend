package ar.edu.utn.frc.k5a.parcial.servicios;

import ar.edu.utn.frc.k5a.parcial.dto.LiquidacionDTO;
import ar.edu.utn.frc.k5a.parcial.excepciones.TarjetaInexistenteException;
import ar.edu.utn.frc.k5a.parcial.modelo.Liquidacion;
import ar.edu.utn.frc.k5a.parcial.modelo.Tarjeta;
import ar.edu.utn.frc.k5a.parcial.repositorios.ConsumoRepository;
import ar.edu.utn.frc.k5a.parcial.repositorios.CotizacionRepository;
import ar.edu.utn.frc.k5a.parcial.repositorios.LiquidacionRepository;
import ar.edu.utn.frc.k5a.parcial.repositorios.TarjetaRepository;

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
        LiquidacionDTO liquidacion = liquidacionService.generarLiquidacion(7, 2026, 5);
        em.getTransaction().commit();
    //INSERT INTO CONSUMOS (ID_TARJETA, MONTO, DIA, MES, ANIO, RUBRO, MONEDA)
        // VALUES (6, 25000.0, 2, 5, 2026, 'OTROS', 'ARS'),
        // (6, 45.0, 10, 5, 2026, 'INDUMENTARIA', 'EUR'),
        // (6, 32000.0, 15, 5, 2026, 'SUPERMERCADO', 'ARS'),
        // (6, 8500.0, 20, 5, 2026, 'COMBUSTIBLE', 'ARS'),
        // (6, 120.0, 28, 5, 2026, 'RESTAURANTES', 'BRL'),
        // (6, 4000.0, 3, 5, 2026, 'OTROS', 'ARS'),
        // (6, 25.0, 6, 5, 2026, 'INDUMENTARIA', 'USD'),
        // (6, 15000.0, 12, 5, 2026, 'SUPERMERCADO', 'ARS'),
        // (6, 6500.0, 18, 5, 2026, 'COMBUSTIBLE', 'ARS'),
        // (6, 80.0, 22, 5, 2026, 'RESTAURANTES', 'EUR'),
        // (6, 12000.0, 25, 5, 2026, 'OTROS', 'ARS'),
        // (6, 35.0, 27, 5, 2026, 'INDUMENTARIA', 'USD');
        assertNotNull(liquidacion);
        assertEquals("4500123412340007", liquidacion.getNumeroTarjeta());
        assertEquals(2026, liquidacion.getAnio());
        assertEquals(5, liquidacion.getMes());
        assertEquals("Diego Lopez", liquidacion.getTitular());

        assertEquals(122500.00, liquidacion.getTotalConsumos(), 0.01);
        assertEquals(21485.00, liquidacion.getTotalImpuestos(), 0.01);
        assertEquals(	13150.00, liquidacion.getTotalDescuentos(), 0.01);
        assertEquals(130835.00, liquidacion.getTotalAPagar(), 0.01);
    }
    @Test
    void totalDescuentosDelPeriodo(){
        em.getTransaction().begin();
        double totalDescuentos = liquidacionService.totalDescuentosDelPeriodo(2026, 5);
        em.getTransaction().commit();
        assertEquals(25650.0, totalDescuentos, 0.01);
        assertEquals(25650.0, totalDescuentos, 0.01);
    }
    @Test
    void numerosSinLiquidar() {
        em.getTransaction().begin();
        List<String> pendientesAntes = liquidacionService.numerosSinLiquidar(2026, 5);
        // data.sql precarga liquidaciones de las tarjetas 1 a 5 para 05/2026: quedan 5 pendientes (6 a 10)
        assertEquals(5, pendientesAntes.size());

        // La tarjeta 6 aun no tiene liquidacion para 05/2026
        liquidacionService.generarLiquidacion(6L, 2026, 5);
        em.getTransaction().commit();

        em.getTransaction().begin();
        List<String> pendientesDespues = liquidacionService.numerosSinLiquidar(2026, 5);
        assertEquals(4, pendientesDespues.size());
        assertFalse(pendientesDespues.contains("4500123412340006"));
        em.getTransaction().commit();
    }
    @Test
    void testGetTarjetasConConsumoEnMoneda(){
        em.getTransaction().begin();
        List<String> tarjetas = liquidacionService.tarjetasConConsumoEnMoneda("EUR", 2026, 5);
        em.getTransaction().commit();
        assertEquals(6, tarjetas.size());                       // son 6
        assertTrue(tarjetas.contains("4500123412340003"));      // la 3 está
        assertTrue(tarjetas.contains("4500123412340001"));      // la 1 está
        assertFalse(tarjetas.contains("4500123412340002"));
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
