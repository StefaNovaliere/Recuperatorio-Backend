package ar.edu.utn.frc.k5a.parcial.repositorios;

import ar.edu.utn.frc.k5a.parcial.modelo.Cotizacion;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CotizacionRepository {

    private EntityManager em;

    public Cotizacion findByMoneda(String moneda) {
        return em.find(Cotizacion.class, moneda);
    }
}
