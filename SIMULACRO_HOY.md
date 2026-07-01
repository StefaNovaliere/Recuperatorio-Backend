# Simulacro final — Recuperatorio Backend 2026

> Simulacro con las **reglas reales** (las mismas que codifica nuestro `data.sql`).
> `schema.sql` y `data.sql` **NO se tocan**. Practicás sobre las tarjetas **6 a 10**,
> que NO tienen liquidación precargada → se calculan de verdad desde los CONSUMOS.
> **Answer key al final (SPOILER).**

## Reglas de negocio (las oficiales del recuperatorio)
La liquidación se expresa siempre en ARS (conversión con `COTIZACIONES`).
El impuesto se calcula sobre el **monto completo** (antes del descuento).

**Descuentos — solo moneda ARS**
- COMBUSTIBLE: 15% con tope de ARS 750 por transacción.
- SUPERMERCADO: 20% con tope de ARS 3000 por transacción.
- RESTAURANTES: 25% para consumos entre el día 10 y el 15 (ambos inclusive).
- INDUMENTARIA / OTROS: sin descuento.
- Moneda extranjera: sin descuentos.

**Impuestos**
- ARS: IVA 21%.
- ARS rubro OTROS: +12% adicional (Resolución BDA 1234/95).
- Moneda extranjera: 7.5% (impuesto extraordinario).

**Cálculo final:** `TOTAL_A_PAGAR = TOTAL_CONSUMOS + TOTAL_IMPUESTOS − TOTAL_DESCUENTOS`

## Consigna
1. Con tu `LiquidacionService`, generá la liquidación de cada tarjeta del CSV nuevo
   (`liquidaciones_hoy.csv`), período 05/2026.
2. Verificá `generarLiquidacion(idTarjeta, 2026, 5)` contra el answer key.
3. Confirmá que `mvn test` sigue en verde.

> Tu código actual (constantes IVA 0.21, OTROS +0.12, extranjera 0.075, comb 15/750,
> super 20/3000, rest 25% días 10-15, sin INDUMENTARIA) **ya implementa estas reglas**.
> O sea: este simulacro es tu ensayo final. Si los números dan, estás listo.

## Nuevo `liquidaciones_hoy.csv` (formato idTarjeta;anio;mes)
```
6;2026;5
7;2026;5
8;2026;5
9;2026;5
10;2026;5
```
(Está también como archivo en `src/main/resources/liquidaciones_hoy.csv`.)

## Casos borde que tocan estas tarjetas
- **Tarjeta 6:** muchos rubros y monedas; INDUMENTARIA extranjera (sin descuento) + RESTAURANTES extranjera.
- **Tarjeta 7:** casi toda ARS; INDUMENTARIA ARS de 25000 → **sin descuento** (no confundir con super/comb).
- **Tarjeta 8:** COMBUSTIBLE de 40000 → descuento **topeado en 750**; RESTAURANTES día 26 → fuera de rango, sin descuento.
- **Tarjeta 9:** dos SUPERMERCADO (15000 y 12000) → 20% sin llegar al tope; OTROS ARS 5000 → paga 21%+12%.
- **Tarjeta 10:** la más variada (ARS/USD/EUR/BRL, 5 rubros) → ideal para chequear conversión y orden de cálculo.

---
---

## SPOILER — Answer key (05/2026)

| Tarjeta | Titular | Consumos | Impuestos | Descuentos | Total a pagar |
|---|---|---|---|---|---|
| 6 | Laura Torres | 439600.00 | 51795.00 | 7500.00 | 483895.00 |
| 7 | Diego Lopez | 122500.00 | 24172.50 | 6100.00 | 140572.50 |
| 8 | Sofia Ramirez | 203400.00 | 31725.00 | 3750.00 | 231375.00 |
| 9 | Jorge Romero | 182650.00 | 22736.25 | 6150.00 | 199236.25 |
| 10 | Lucia Fernandez | 473700.00 | 62910.00 | 3100.00 | 533510.00 |

Si un total no da, revisá en este orden:
1. Conversión de moneda (¿tasa de COTIZACIONES correcta?).
2. Impuesto (¿+12% solo a OTROS ARS? ¿7.5% solo a extranjera? ¿INDUMENTARIA NO lleva descuento?).
3. Descuento (¿topes de 750/3000? ¿RESTAURANTES solo días 10-15?).
4. Orden: impuesto sobre el monto completo; el total resta el descuento.
