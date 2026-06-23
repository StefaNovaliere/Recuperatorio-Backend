package ar.edu.utn.frc.k5a.parcial.dto;

public class LiquidacionDTO {
    private Long id;
    private String numeroTarjeta;
    private String titular;
    private int mes;
    private int anio;
    private double totalAPagar;
    private double totalConsumos;
    private double totalImpuestos;
    private double totalDescuentos;

    public LiquidacionDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroTarjeta() {
        return numeroTarjeta;
    }

    public void setNumeroTarjeta(String numeroTarjeta) {
        this.numeroTarjeta = numeroTarjeta;
    }

    public String getTitular() {
        return titular;
    }

    public void setTitular(String titular) {
        this.titular = titular;
    }

    public int getMes() {
        return mes;
    }

    public void setMes(int mes) {
        this.mes = mes;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public double getTotalAPagar() {
        return totalAPagar;
    }

    public void setTotalAPagar(double totalAPagar) {
        this.totalAPagar = totalAPagar;
    }

    public double getTotalConsumos() {
        return totalConsumos;
    }

    public void setTotalConsumos(double totalConsumos) {
        this.totalConsumos = totalConsumos;
    }

    public double getTotalImpuestos() {
        return totalImpuestos;
    }

    public void setTotalImpuestos(double totalImpuestos) {
        this.totalImpuestos = totalImpuestos;
    }

    public double getTotalDescuentos() {
        return totalDescuentos;
    }

    public void setTotalDescuentos(double totalDescuentos) {
        this.totalDescuentos = totalDescuentos;
    }

    @Override
    public String toString() {
        return "LiquidacionDTO{" +
                "id=" + id +
                ", numeroTarjeta='" + numeroTarjeta + '\'' +
                ", titular='" + titular + '\'' +
                ", mes=" + mes +
                ", anio=" + anio +
                ", totalAPagar=" + totalAPagar +
                ", totalConsumos=" + totalConsumos +
                ", totalImpuestos=" + totalImpuestos +
                ", totalDescuentos=" + totalDescuentos +
                '}';
    }
}
