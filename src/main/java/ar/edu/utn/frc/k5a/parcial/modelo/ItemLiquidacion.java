package ar.edu.utn.frc.k5a.parcial.modelo;

public class ItemLiquidacion {

    private double consumos;
    private double impuestos;
    private double descuentos;

    public ItemLiquidacion() {
        this(0, 0, 0);
    }

    public ItemLiquidacion(double consumos, double impuestos, double descuentos) {
        this.consumos = consumos;
        this.impuestos = impuestos;
        this.descuentos = descuentos;
    }

    public ItemLiquidacion acumular(ItemLiquidacion otro) {
        return new ItemLiquidacion(
            consumos + otro.consumos,
            impuestos + otro.impuestos,
            descuentos + otro.descuentos
        );

    }

    public double getTotal() {
        return consumos + impuestos - descuentos;
    }

    public double getConsumos() {
        return consumos;
    }

    public double getImpuestos() {
        return impuestos;
    }

    public double getDescuentos() {
        return descuentos;
    }

    @Override
    public String toString() {
        return "ItemLiquidacion{" +
                "consumos=" + consumos +
                ", impuestos=" + impuestos +
                ", descuentos=" + descuentos +
                '}';
    }
}
