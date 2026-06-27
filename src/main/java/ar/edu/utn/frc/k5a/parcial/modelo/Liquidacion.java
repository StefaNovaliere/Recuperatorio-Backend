package ar.edu.utn.frc.k5a.parcial.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "LIQUIDACIONES")
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "ID_TARJETA", nullable = false)
    private Tarjeta tarjeta;

    @Column(name = "MES", nullable = false)
    private int mes;

    @Column(name = "ANIO", nullable = false)
    private int anio;

    @Column(name = "TOTAL_A_PAGAR", nullable = false)
    private double totalAPagar;

    @Column(name = "TOTAL_CONSUMOS", nullable = false)
    private double totalConsumos;

    @Column(name = "TOTAL_IMPUESTOS", nullable = false)
    private double totalImpuestos;

    @Column(name = "TOTAL_DESCUENTOS", nullable = false)
    private double totalDescuentos;
}
