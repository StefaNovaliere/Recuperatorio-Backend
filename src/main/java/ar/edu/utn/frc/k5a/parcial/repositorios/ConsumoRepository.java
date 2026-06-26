package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Consumo;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ConsumoRepository {

    private EntityManager em;

    public List<Consumo> findByTarjetaYPeriodo(long idTarjeta, int anio, int mes) {
        String jpql = "SELECT c FROM Consumo c WHERE c.tarjeta.id = :idTarjeta " +
                      "AND c.anio = :anio AND c.mes = :mes";
        return em.createQuery(jpql, Consumo.class)
                 .setParameter("idTarjeta", idTarjeta)
                 .setParameter("anio", anio)
                 .setParameter("mes", mes)
                 .getResultList();
    }
}
