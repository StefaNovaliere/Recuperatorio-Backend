package ar.edu.utn.frc.k5a.parcial.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "CONSUMOS")
public class Consumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_TARJETA", nullable = false)
    private Tarjeta tarjeta;

    @Column(name = "MONTO", nullable = false)
    private double monto;

    @Column(name = "DIA", nullable = false)
    private int dia;

    @Column(name = "MES", nullable = false)
    private int mes;

    @Column(name = "ANIO", nullable = false)
    private int anio;

    @Column(name = "RUBRO", nullable = false, length = 20)
    private String rubro;

    @Column(name = "MONEDA", nullable = false, length = 3)
    private String moneda;
}
