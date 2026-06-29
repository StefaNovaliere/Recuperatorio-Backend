package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Liquidacion;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;

//public List<Consumo> buscarPorTarjetaYPeriodo(String numeroTarjeta, int anio, int mes) {
//        String jpql = "SELECT c FROM Consumo c WHERE c.tarjeta.numero = :numero " +
//                "AND c.anio = :anio AND c.mes = :mes";
//        return em.createQuery(jpql, Consumo.class)
//                .setParameter("numero", numeroTarjeta)
//                .setParameter("anio", anio)
//                .setParameter("mes", mes)
//                .getResultList();
//    }

@AllArgsConstructor
public class LiquidacionRepository {

    private EntityManager em;

    public void guardar(Liquidacion liquidacion) {
        em.persist(liquidacion);
    }
    public Optional<Liquidacion> buscarPorTarjetaYPeriodo(String numero, int anio, int mes){
        String jpql = "SELECT c FROM Liquidacion c WHERE c.Liquidacion.numero = :numero " +
                "AND c.anio = :anio and c.mes = :mes";
        return em.createQuery(jpql, Liquidacion.class)
                .setParameter("numero", numero)
                .setParameter("anio", anio)
                .setParameter("mes", mes);
    }
    public <SELECT> List<Liquidacion> totalLiquidadoXMes(int mes){
        SELECT SUM(c.totalAPagar) FROM Liquidacion c where c.mes = :mes
    }
}