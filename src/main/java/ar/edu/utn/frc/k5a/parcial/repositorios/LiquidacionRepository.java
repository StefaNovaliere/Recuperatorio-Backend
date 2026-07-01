package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Liquidacion;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class LiquidacionRepository {

    private EntityManager em;

    public void guardar(Liquidacion liquidacion) {
        em.persist(liquidacion);
    }

    // Suma de descuentos de todas las liquidaciones del periodo
    public double totalDescuentosDelPeriodo(int anio, int mes) {
        String jpql = "SELECT SUM(l.totalDescuentos) FROM Liquidacion l WHERE l.anio = :anio AND l.mes = :mes";
        Double total = em.createQuery(jpql, Double.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
        if (total == null) {
            return 0.0;
        }
        return total;
    }

    // Suma de los totales a pagar de todas las liquidaciones del periodo
    public double totalAPagarDelPeriodo(int anio, int mes) {
        String jpql = "SELECT SUM(l.totalAPagar) FROM Liquidacion l WHERE l.anio = :anio AND l.mes = :mes";
        Double total = em.createQuery(jpql, Double.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
        if (total == null) {
            return 0.0;
        }
        return total;
    }

    // Cantidad de liquidaciones cargadas en el periodo
    public long contarLiquidacionesPeriodo(int anio, int mes) {
        String jpql = "SELECT COUNT(l) FROM Liquidacion l WHERE l.anio = :anio AND l.mes = :mes";
        Long cantidad = em.createQuery(jpql, Long.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
        return cantidad;
    }

    // Todas las liquidaciones del periodo
    public List<Liquidacion> buscarLiquidacionPeriodo(int anio, int mes) {
        String jpql = "SELECT l FROM Liquidacion l WHERE l.anio = :anio AND l.mes = :mes";
        return em.createQuery(jpql, Liquidacion.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getResultList();
    }

    // La liquidacion de una tarjeta (por NUMERO) para un anio/mes especifico
    public Optional<Liquidacion> buscarPorTarjetaYPeriodo(String numero, int anio, int mes) {
        String jpql = "SELECT l FROM Liquidacion l WHERE l.tarjeta.numero = :numero " +
                "AND l.anio = :anio AND l.mes = :mes";
        return em.createQuery(jpql, Liquidacion.class)
                .setParameter("numero", numero)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getResultStream()
                .findFirst();
    }
}
