# SIMULACRO 2 — Etapa Presencial (Recuperatorio)

> Segundo parcial de práctica. Reglas nuevas, un rename, un método nuevo y otro formato de CSV.
> **Respuestas al final (SPOILER).** No mires hasta terminar.

## Preparación
- Trabajá en una branch: `git checkout -b simulacro2`.
- Recordá: para probar reglas de cálculo, usá una tarjeta SIN liquidación precargada (6 a 10),
  porque la idempotencia devuelve la precargada de las tarjetas 1 a 5.

---

## Consigna 1 — Nuevas reglas de cálculo
Reimplementá `calcularConsumo` (todo convertido a ARS; impuesto sobre el monto completo):

**Impuestos**
- ARS: IVA **18%**.
- ARS rubro **OTROS**: adicional **15%**.
- Moneda extranjera: **10%**.

**Descuentos (solo ARS)**
- SUPERMERCADO: **25%**, tope **5000**.
- COMBUSTIBLE: **12%**, tope **1200**.
- RESTAURANTES: **30%**, **todos los días** (ya no importa el día).
- INDUMENTARIA: sin descuento.

Verificá con la **tarjeta 7, 05/2026** (ver answer key).

## Consigna 2 — Método nuevo (reporte)
Agregá al servicio:
```java
double totalDescuentosDelPeriodo(int anio, int mes)
```
Devuelve la **suma de `TOTAL_DESCUENTOS`** de todas las liquidaciones de ese año/mes.
> Pista: consulta nueva en `LiquidacionRepository` con `SELECT SUM(l.totalDescuentos) ...`.
> Ojo: `SUM` devuelve `null` si no hay filas → devolvé 0 en ese caso.

Verificá con `(2026, 5)` (ver answer key).

## Consigna 3 — Renombran una firma
La cátedra renombró en la interfaz:
`getLiquidacionesPendientes(int anio, int mes)` → **`numerosSinLiquidar(int anio, int mes)`**
(mismo comportamiento). Actualizá donde corresponda.
> ¿Qué archivos toca? (Impl + tu llamada en el test. Los repos NO.)

## Consigna 4 — Nuevo formato de CSV
El nuevo `liquidaciones.csv` usa **pipe `|`** como separador y trae **encabezado**:
```
idTarjeta|anio|mes
8|2026|5
9|2026|5
10|2026|5
```
Adaptá `liquidarLote`: separador `|` y saltear el header.
> ⚠️ Trampa: `split("|")` NO funciona como esperás — `|` es un carácter especial de regex.
> Hay que escaparlo: `split("\\|")`. Debe devolver 3 liquidaciones.

---
---

## SPOILER — Answer key

**Consigna 1 — tarjeta 7, 05/2026 (reglas nuevas):**
| | Valor |
|---|---|
| totalConsumos | 122500.00 |
| totalImpuestos | 21485.00 |
| totalDescuentos | 13150.00 |
| totalAPagar | 130835.00 |

Chequeos rápidos: OTROS ARS (4500) impuesto = 18%+15% = 33% → 1485. RESTAURANTES (5000 y 7500) descuento 30% siempre → 1500 + 2250. INDUMENTARIA ARS (25000) → sin descuento. La tarjeta 7 casi no tiene extranjera (solo 10 USD → 15500 ARS × 10% = 1550 de impuesto).

**Consigna 2 — `totalDescuentosDelPeriodo(2026, 5)` = `25650.0`**
(Suma de las liquidaciones de mayo: 4950 + 6750 + 750 + 3000 + 10200. La de tarjeta 4 de **abril** no cuenta.)

**Consigna 3 —** `numerosSinLiquidar(2026, 5)` sigue devolviendo **5** (tarjetas 6 a 10). El rename se toca en interfaz (te la dan), `LiquidacionServiceImpl` (@Override) y la llamada del test. Repos NO.

**Consigna 4 —** con el CSV nuevo → lista de **3** LiquidacionDTO. Clave: `linea.split("\\|")` (escapado) y descartar la línea del header.
