# Simulacro — Recuperatorio Backend de Aplicaciones 2026

## Introducción

Este es un **simulacro** pensado para practicar de cara al recuperatorio del parcial de generación de
liquidaciones de tarjetas de crédito. La consigna, la estructura de la base de datos y las reglas de
negocio son las mismas que las del parcial original (Backend de Aplicaciones 2026). Lo único que cambia
respecto de esa instancia es el archivo `liquidaciones.csv` que se entrega junto a este enunciado: contiene
otras combinaciones de tarjeta/año/mes a resolver, distintas de las que se usaron en el parcial.

**Importante:** para este simulacro se sigue usando el mismo `schema.sql` y el mismo `data.sql` que ya
tenés (no fue modificado). La idea es que practiques exactamente sobre los datos que ya conocés, pero
resolviendo combinaciones de tarjeta/mes que **no** tienen una liquidación precargada en la tabla
`LIQUIDACIONES`, para forzarte a calcularlas realmente a partir de los `CONSUMOS`.

## Estructura de la base de datos

Sin cambios respecto del enunciado original:

### COTIZACIONES
Una fila por cada tasa de cambio entre el Peso Argentino y otra moneda (por ejemplo, USD = 1550 significa
que 1 USD equivale a ARS 1.550).

### TARJETAS
- `ID`: autoincremental
- `NUMERO`: número de tarjeta
- `TITULAR`: nombre del titular
- `LIMITE_CREDITO`: límite de crédito mensual

### CONSUMOS
- `ID`: autoincremental
- `ID_TARJETA`: id de la tarjeta asociada
- `MONTO`: monto del consumo
- `DIA`, `MES`, `ANIO`: fecha del consumo
- `RUBRO`: categoría del consumo (SUPERMERCADO, COMBUSTIBLE, RESTAURANTES, INDUMENTARIA, OTROS)
- `MONEDA`: código de moneda del consumo (ARS, USD, EUR, BRL, CLP)

### LIQUIDACIONES
Una fila por cada liquidación ya calculada, para un mes completo (del primer al último día del mes).

## Reglas de negocio (sin cambios)

La liquidación se expresa siempre en ARS. Las conversiones se hacen con la tasa de cambio vigente en
`COTIZACIONES`.

### Descuentos

**Moneda ARS**
- COMBUSTIBLE: 15% con tope de ARS 750 por transacción
- SUPERMERCADO: 20% con tope de ARS 3.000 por transacción
- RESTAURANTES: 25% para consumos entre el día 10 y el día 15 de cada mes (ambos inclusive)

**Moneda extranjera (cualquiera que no sea ARS)**
- No hay descuentos

### Impuestos

Los impuestos se calculan siempre sobre el monto completo (sin aplicar el descuento).

**Moneda ARS**
- IVA: 21%
- Resolución BDA Nro. 1234/95: 12% adicional, solo para el rubro OTROS

**Moneda extranjera**
- Impuesto extraordinario: 7,5%

### Cálculo final

```
TOTAL_CONSUMOS   = Σ (monto del consumo convertido a ARS)
TOTAL_IMPUESTOS  = Σ (impuesto de cada consumo, según su moneda y rubro)
TOTAL_DESCUENTOS = Σ (descuento de cada consumo, según su moneda, rubro y fecha)
TOTAL_A_PAGAR    = TOTAL_CONSUMOS + TOTAL_IMPUESTOS - TOTAL_DESCUENTOS
```

## Consigna del simulacro

1. Usando tu implementación de `LiquidacionService` desarrollada para el parcial (o la que estés
   preparando para el recuperatorio), calculá la liquidación de cada combinación tarjeta/año/mes
   indicada en el nuevo `liquidaciones.csv` adjunto.
2. Ninguna de esas combinaciones tiene una fila precargada en `LIQUIDACIONES`: tenés que generarlas a
   partir de los `CONSUMOS` correspondientes, aplicando las reglas de descuentos e impuestos.
3. Verificá que tu implementación siga pasando los tests unitarios que ya tenías del parcial (`mvn
   test`), y adaptá o agregá los casos necesarios para cubrir estas nuevas combinaciones.
4. Como ejercicio extra de repaso, fijate que estas tarjetas tocan casos borde interesantes:
    - Tarjeta 8: tiene un consumo de INDUMENTARIA muy alto en ARS sin tope de descuento (a diferencia de
      COMBUSTIBLE/SUPERMERCADO, INDUMENTARIA no tiene descuento).
    - Tarjeta 9: tiene un consumo de SUPERMERCADO que llega justo al tope de descuento y otro que no.
    - Tarjeta 10: es la tarjeta con más rubros y monedas distintas combinadas en un mismo mes, buena para
      verificar la conversión de moneda y el orden de cálculo (impuesto sobre el monto sin descontar).

## Nuevo `liquidaciones.csv`

Reemplazá el `liquidaciones.csv` que tenías por este (formato `ID_TARJETA;ANIO;MES`):

```
7;2026;05
8;2026;05
9;2026;05
10;2026;05
```

## Cómo autoevaluarte

Una vez que tengas los resultados de tu implementación, comparalos contra la planilla de respuestas
correctas que te adjunto por separado (`Solucion-Simulacro.md`). Si algún total no coincide, revisá en
este orden:
1. La conversión de moneda (¿usaste la tasa de `COTIZACIONES` correcta?).
2. El impuesto (¿aplicaste el 12% extra solo a OTROS en ARS? ¿el 7,5% solo a moneda extranjera?).
3. El descuento (¿respetaste los topes? ¿el rango de días 10–15 inclusive para RESTAURANTES?).
4. El orden de las operaciones: impuesto sobre el monto completo, descuento aparte, y el total final
   resta el descuento y suma el impuesto sobre el consumo en ARS.