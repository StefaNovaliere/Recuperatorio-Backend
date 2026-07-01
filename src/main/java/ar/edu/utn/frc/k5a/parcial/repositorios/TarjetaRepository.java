package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Tarjeta;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class TarjetaRepository {

    private EntityManager em;

    public Optional<Tarjeta> buscarPorId(long id) {
        return Optional.ofNullable(em.find(Tarjeta.class, id));
    }

    // Tarjetas que NO tienen liquidacion para el anio/mes dado
    public List<Tarjeta> buscarSinLiquidacion(int anio, int mes) {
        String jpql = "SELECT t FROM Tarjeta t WHERE t NOT IN " +
                "(SELECT l.tarjeta FROM Liquidacion l WHERE l.anio = :anio AND l.mes = :mes)";
        return em.createQuery(jpql, Tarjeta.class)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getResultList();
    }
    public long tarjetasQueGastaronMasDe(double monto, int anio, int mes){
        String jpql = "SELECT COUNT(l) FROM Liquidacion l WHERE l.totalConsumos > :monto AND l.anio = :anio AND l.mes = :mes";
        Long total = em.createQuery(jpql, Long.class)
                .setParameter("monto", monto)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
        return total;

    }
}
