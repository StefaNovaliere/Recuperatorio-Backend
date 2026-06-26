package ar.edu.utn.frc.k5a.parcial.modelo;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "TARJETAS")
public class Tarjeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NUMERO", nullable = false, unique = true, length = 16)
    private String numero;

    @Column(name = "TITULAR", nullable = false, length = 100)
    private String titular;

    @Column(name = "LIMITE_CREDITO", nullable = false)
    private double limiteCredito;

    @OneToMany(mappedBy = "tarjeta")
    @ToString.Exclude
    private List<Consumo> consumos;

    @OneToMany(mappedBy = "tarjeta")
    @ToString.Exclude
    private List<Liquidacion> liquidaciones;
}
