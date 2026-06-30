package ar.edu.utn.frc.k5a.parcial.modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "COTIZACIONES")
public class Cotizacion {

    @Id
    @Column(name = "MONEDA", length = 3)
    private String moneda;

    @Column(name = "TASA_CAMBIO", nullable = false)
    private double tasaCambio;
}
