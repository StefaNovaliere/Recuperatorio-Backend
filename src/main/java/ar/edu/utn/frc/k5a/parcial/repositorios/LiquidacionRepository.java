package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Liquidacion;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.Optional;

@AllArgsConstructor
public class LiquidacionRepository {

    private EntityManager em;

    public void guardar(Liquidacion liquidacion) {
        em.persist(liquidacion);
    }

    // Consulta #3 del enunciado: la liquidacion de una tarjeta para un anio/mes,
    // buscando por NUMERO de tarjeta.
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

    // Reporte: total liquidado (suma de TOTAL_A_PAGAR) de un periodo.
    public double totalLiquidadoPorPeriodo(int anio, int mes) {
        String jpql = "SELECT SUM(l.totalAPagar) FROM Liquidacion l " +
                "WHERE l.anio = :anio AND l.mes = :mes";
        Double total = em.createQuery(jpql, Double.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
        return total != null ? total : 0.0;
    }
}
