package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Consumo;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ConsumoRepository {

    private EntityManager em;

    // Consumos de una tarjeta (por NUMERO) para un anio y mes especifico
    public List<Consumo> buscarPorTarjetaYPeriodo(String numeroTarjeta, int anio, int mes) {
        String jpql = "SELECT c FROM Consumo c WHERE c.tarjeta.numero = :numero " +
                "AND c.anio = :anio AND c.mes = :mes";
        return em.createQuery(jpql, Consumo.class)
                .setParameter("numero", numeroTarjeta)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getResultList();
    }
}
