package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Consumo;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class ConsumoRepository {

    private EntityManager em;
    public List<Consumo> buscarPorMonedaYFecha(String moneda, int anio, int mes){
        String jpql = "SELECT c FROM Consumo c WHERE c.moneda = :moneda " +
                "AND c.anio = :anio AND c.mes = :mes";
        return em.createQuery(jpql, Consumo.class)
                .setParameter("moneda", moneda)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getResultList();
    }
    public List<Consumo> monedasUsadasPorTarjeta(String numero, int anio, int mes){
        String jpql = "SELECT DISTINCT c.moneda FROM Consumo c WHERE c.tarjeta.numero = :numero AND c.anio = :anio AND c.mes = :mes";
        return em.createQuery(jpql, Consumo.class)
                .setParameter("numero", numero)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getResultList();
    }
    public double totalConsumidoEnMoneda(String moneda, int anio, int mes){
        String jpql = "SELECT SUM(c.monto) FROM Consumo c WHERE c.moneda = :moneda AND c.anio = :anio  AND c.mes = :mes ";
        Double total = em.createQuery(jpql, Double.class)
                .setParameter("moneda", moneda)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
        if (total == null){
            return 0;
        }
        return total;
    }
    public long contarConsumosDeTarjeta(String numero, int anio, int mes){
        String jpql = "SELECT COUNT(c) FROM Consumo c WHERE c.tarjeta.numero = :numero" +
                " AND c.anio = :anio AND c.mes = :mes";
        Long total = em.createQuery(jpql, Long.class)
                .setParameter("numero", numero)
                .setParameter("anio", anio)
                .setParameter("mes", mes)
                .getSingleResult();
        return total;
    }
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
