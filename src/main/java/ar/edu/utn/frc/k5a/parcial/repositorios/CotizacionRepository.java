package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Cotizacion;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class CotizacionRepository {

    private EntityManager em;

    public Map<String, Double> obtenerCotizaciones() {
        return em.createQuery("SELECT c FROM Cotizacion c", Cotizacion.class)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(Cotizacion::getMoneda, Cotizacion::getTasaCambio));
    }
}