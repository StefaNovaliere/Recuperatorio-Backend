# SIMULACRO — Etapa Presencial (Recuperatorio)

> Ejercicio de práctica. Simula lo que la cátedra podría entregarte el día del examen:
> cambia reglas, renombra una firma, pide un método nuevo y cambia el CSV.
> **Las respuestas están al final (SPOILER).** No mires hasta terminar.

## Preparación (antes de empezar)
- Guardá tu solución actual: `git checkout -b simulacro` (trabajás en una branch y después volvés).
- Recordá: al cambiar reglas, **los valores esperados del test cambian** → vas a tener que actualizarlos con los del answer key.

---

## Consigna 1 — Nuevas reglas de cálculo
Reimplementá `calcularConsumo` con estas reglas (todo convertido a ARS; impuesto sobre el monto completo):

**Impuestos**
- ARS: IVA **21%** a todos los consumos.
- ARS rubro **OTROS**: adicional **10%** (antes 12%).
- Moneda extranjera: **8%** (antes 7.5%).

**Descuentos (solo ARS)**
- COMBUSTIBLE: **10%**, tope **1000** (antes 15%/750).
- SUPERMERCADO: **20%**, tope **3000** (igual).
- RESTAURANTES: **20%** entre los días **1 y 10** (antes 25% días 10–15).
- INDUMENTARIA: **5%**, tope **500** (regla NUEVA).

Verificá con la **tarjeta 1, 05/2026** (ver answer key).

## Consigna 2 — Renombran una firma
La cátedra renombró en la interfaz:
`getLiquidacionesPendientes(int anio, int mes)` → **`tarjetasSinLiquidacion(int anio, int mes)`**
(mismo comportamiento). Actualizá donde corresponda.
> Pista: ¿en qué archivos toca un rename de la interfaz? (Impl + tu llamada en el test; los repos NO.)

## Consigna 3 — Método nuevo
Agregá al servicio:
```java
List<String> tarjetasConConsumoEnMoneda(String moneda, int anio, int mes)
```
Devuelve el **número** de las tarjetas que tienen **al menos un consumo** en esa moneda, en ese año/mes.
> Pista: necesitás una consulta nueva en un repositorio (¿cuál?). JPQL con `DISTINCT` sobre CONSUMOS.

Verificá con `("EUR", 2026, 5)` (ver answer key).

## Consigna 4 — Nuevo formato de CSV
El nuevo `liquidaciones.csv` ahora usa **coma** y trae **encabezado**:
```
idTarjeta,anio,mes
7,2026,5
8,2026,5
9,2026,5
```
Adaptá `liquidarLote`: separador `,`, **saltear la primera línea** (header).
> Debe devolver 3 liquidaciones.

---
---

## SPOILER — Answer key (mirá recién al terminar)

**Consigna 1 — con las reglas nuevas:**
| Tarjeta (05/2026) | Consumos | Impuestos | Descuentos | Total a pagar |
|---|---|---|---|---|
| 1 | 436400.00 | 45702.00 | 7550.00 | 474552.00 |
| 2 | 172625.00 | 27070.00 | 7000.00 | 192695.00 |
| 4 | 83750.00 | 12550.00 | 3000.00 | 93300.00 |

(Los consumos no cambian porque la conversión es la misma; cambian impuestos y descuentos.)

**Consigna 2:** `tarjetasSinLiquidacion(2026, 5)` sigue devolviendo **5** (las precargadas 1–5 no cambian). El rename se toca en: interfaz (te la dan), `LiquidacionServiceImpl` (@Override) y la llamada en el test. Repos NO.

**Consigna 3:** `tarjetasConConsumoEnMoneda("EUR", 2026, 5)` → **6** tarjetas:
4500123412340001, 4500123412340003, 4500123412340006, 4500123412340008, 4500123412340009, 4500123412340010.

**Consigna 4:** con el CSV nuevo (cards 7, 8, 9) → lista de **3** LiquidacionDTO.
Clave del código: `linea.split(",")` y saltear la línea del header (por ej. `if (linea.startsWith("idTarjeta")) continue;` o descartar la primera).
