package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Tarjeta;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TarjetaRepository {

    private EntityManager em;

    public Tarjeta findById(long id) {
        return em.find(Tarjeta.class, id);
    }

    public List<Tarjeta> findAll() {
        return em.createQuery("SELECT t FROM Tarjeta t", Tarjeta.class)
                 .getResultList();
    }

    public List<Tarjeta> findSinLiquidacion(int anio, int mes) {
        String jpql = "SELECT t FROM Tarjeta t WHERE t NOT IN " +
                      "(SELECT l.tarjeta FROM Liquidacion l WHERE l.anio = :anio AND l.mes = :mes)";
        return em.createQuery(jpql, Tarjeta.class)
                 .setParameter("anio", anio)
                 .setParameter("mes", mes)
                 .getResultList();
    }
}
