package ar.edu.utn.frc.k5a.parcial.servicios;

import ar.edu.utn.frc.k5a.parcial.dto.LiquidacionDTO;
import ar.edu.utn.frc.k5a.parcial.excepciones.TarjetaInexistenteException;
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
    void testBuscarLiquidacionPeriodo(){
        em.getTransaction().begin();
        List<LiquidacionDTO> liquidaciones = liquidacionService.buscarLiquidacionPeriodo(2026, 5);
        em.getTransaction().commit();

    }
    @Test
    void testContarLiquidacionesPeriodo(){
        em.getTransaction().begin();
        long cantidadMayo = liquidacionService.contarLiquidacionesPeriodo(2026, 5);
        long cantidadAbril = liquidacionService.contarLiquidacionesPeriodo(2026, 4);
        em.getTransaction().commit();
        assertEquals(5L, cantidadMayo, "Para Mayo debería haber 5 liquidaciones");
        assertEquals(1L, cantidadAbril, "Para Abril debería haber 0 liquidaciones");
    }
    @Test
    void testTotalAPagarPeriodo(){
        em.getTransaction().begin();
        double total = liquidacionService.totalAPagarDelPeriodo(2026, 5);
        em.getTransaction().commit();
        assertEquals(1584130.63, total, "Dio el resultado esperado 1584130.63");
    }
    @Test
    void testContarConsumosTarjeta(){
        em.getTransaction().begin();
        long cantidadConsumos = liquidacionService.contarConsumosDeTarjeta("4500123412340001", 2026, 5);
        em.getTransaction().commit();
        assertEquals(15L, cantidadConsumos);
    }
    @Test
    void testCantidadConsumidoMoneda(){
        em.getTransaction().begin();
        double cantidadConsumidaMoneda = liquidacionService.totalConsumidoEnMoneda("USD", 2026, 5);
        em.getTransaction().commit();
        assertEquals(532.5, cantidadConsumidaMoneda, 0.01);
    }
    @Test
    void testMonedasUsadasPorTarjeta(){
        em.getTransaction().begin();
        List<String> monedas = liquidacionService.monedasUsadasPorTarjeta("4500123412340006",2026, 5);
        em.getTransaction().commit();
        assertEquals(4L, monedas.size());
    }
    @Test
    void testTarjetasGastaronMasDe(){
        em.getTransaction().begin();
        long tarjetasExcedidas = liquidacionService.tarjetasQueGastaronMasDe(40000, 2026, 5);
        em.getTransaction().commit();
        assertEquals(5L, tarjetasExcedidas);
    }
    @Test
    void testGenerarLiquidacion() {
        em.getTransaction().begin();
        LiquidacionDTO liquidacion = liquidacionService.generarLiquidacion(7, 2026, 5);
        em.getTransaction().commit();

        assertNotNull(liquidacion);
        assertEquals("4500123412340007", liquidacion.getNumeroTarjeta());
        assertEquals(2026, liquidacion.getAnio());
        assertEquals(5, liquidacion.getMes());
        assertEquals("Diego Lopez", liquidacion.getTitular());

        assertEquals(122500.00, liquidacion.getTotalConsumos(), 0.01);
        assertEquals(24172.50, liquidacion.getTotalImpuestos(), 0.01);
        assertEquals(6100.00, liquidacion.getTotalDescuentos(), 0.01);
        assertEquals(140572.50, liquidacion.getTotalAPagar(), 0.01);
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
        // ── VARIANTES DE liquidarLote (por si cambian el formato del CSV) ──
        // 1) Separador coma  → en el Impl: linea.split(",")
        // 2) Separador pipe  → en el Impl: linea.split("\\|")   // ¡el pipe es regex, va escapado!
        // 3) Con encabezado  → en el Impl: .filter(l -> !l.startsWith("id"))  // saltea la 1ª línea
        // Para probar otro archivo: cambio SOLO el argumento de getResource(...) abajo,
        // el resto del test queda igual.
    void testLiquidarLote() throws IOException {
        URL url = getClass().getClassLoader().getResource("liquidaciones_hoy.csv");
        assertNotNull(url, "No se encontró el archivo de lotes liquidaciones_hoy.csv");

        em.getTransaction().begin();
        List<LiquidacionDTO> liquidaciones = liquidacionService.liquidarLote(url.getPath());
        em.getTransaction().commit();

        assertFalse(liquidaciones.isEmpty());
    }
}
