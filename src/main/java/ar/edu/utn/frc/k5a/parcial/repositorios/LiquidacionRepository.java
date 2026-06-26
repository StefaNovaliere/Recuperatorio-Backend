package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Liquidacion;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class LiquidacionRepository {

    private EntityManager em;

    public void guardar(Liquidacion liquidacion) {
        em.persist(liquidacion);
    }

    public Liquidacion findByNumeroTarjetaYPeriodo(String numeroTarjeta, int anio, int mes) {
        String jpql = "SELECT l FROM Liquidacion l WHERE l.tarjeta.numero = :numero " +
                      "AND l.anio = :anio AND l.mes = :mes";
        List<Liquidacion> resultados = em.createQuery(jpql, Liquidacion.class)
                 .setParameter("numero", numeroTarjeta)
                 .setParameter("anio", anio)
                 .setParameter("mes", mes)
                 .getResultList();
        return resultados.isEmpty() ? null : resultados.get(0);
    }
}
