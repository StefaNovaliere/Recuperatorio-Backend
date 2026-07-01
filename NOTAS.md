# Notas para el Recuperatorio — Backend 2026

## 0. Antes de empezar (2 minutos)
1. Abrí el `liquidaciones.csv` → mirá el **separador** (`;`) y si tiene **encabezado**.
2. Leé del PDF las **reglas de cálculo** (porcentajes, topes, condiciones) y cargalas en las **constantes**.
3. Confirmá que `mvn test` arranca (aunque falle) → la base se recrea sola en cada test.

## 1. Qué tenés listo (preparación — NO se toca)
- **Entidades** (`modelo/`): Cotizacion, Tarjeta, Consumo, Liquidacion.
- **Repositorios** (`repositorios/`) con las 3 consultas del enunciado:
  - `ConsumoRepository.buscarPorTarjetaYPeriodo(numero, anio, mes)` → consumos por período.
  - `TarjetaRepository.buscarSinLiquidacion(anio, mes)` → tarjetas sin liquidación (= pendientes).
  - `LiquidacionRepository.buscarPorTarjetaYPeriodo(numero, anio, mes)` → liquidación existente.
  - + `CotizacionRepository.obtenerCotizaciones()`, `TarjetaRepository.buscarPorId(id)`, `LiquidacionRepository.guardar(l)`.
- `persistence.xml` (drop-and-create), `data.sql`, `schema.sql`, DTO, excepción, ItemLiquidacion.

## 2. Qué implementás (presencial) → SOLO `LiquidacionServiceImpl`
Las 3 firmas de la interfaz:
- `LiquidacionDTO generarLiquidacion(long idTarjeta, int anio, int mes)`
- `List<String> getLiquidacionesPendientes(int anio, int mes)`
- `List<LiquidacionDTO> liquidarLote(String rutaArchivo)`

Flujo de `generarLiquidacion`:
1. `buscarPorId` → si vacío, `throw TarjetaInexistenteException`.
2. (opcional idempotencia) `buscarPorTarjetaYPeriodo` → si existe, `return map(...)`.
3. `buscarPorTarjetaYPeriodo` consumos → `for` acumulando con `ItemLiquidacion.acumular`.
4. `new Liquidacion(tarjeta, anio, mes)` + setear totales → `guardar` → `map` a DTO.

## 3. Reglas (las del parcial — pueden cambiar los números mañana)
Impuesto SOBRE EL MONTO COMPLETO (antes del descuento). Todo convertido a ARS.
- **ARS**: IVA 21% a todo. Rubro **OTROS** suma +12% (Resolución BDA).
- **Extranjera**: 7.5% sobre el monto convertido (× cotización). Sin descuentos.
- **Descuentos (solo ARS)**: COMBUSTIBLE 15% tope 750 · SUPERMERCADO 20% tope 3000 · RESTAURANTES 25% solo días 10–15.

## 4. CSV (`liquidarLote`)
- Formato `idTarjeta;anio;mes`, separador **`;`**, sin encabezado.
- Pasos: leer líneas → `split(";")` → `Long/Integer.parseLong/parseInt(campos[i].trim())` → llamar a `generarLiquidacion` → juntar en lista.
- Ojo Windows: `url.getPath()` da `/C:/...` → sacar la barra inicial.
- Reutilizar `generarLiquidacion`, NO recalcular.

## 5. "Firma → dónde toco" (si renombran un método)
- Método de **interfaz** renombrado → tocás el `@Override` en `LiquidacionServiceImpl` + tus llamadas internas (ej. dentro de `liquidarLote`). Repos y test NO.
- Método de **repositorio** renombrado → tocás solo las llamadas en el servicio.
- Tip: IntelliJ `Shift+F6` (Rename) actualiza todos los usos de una.

## 6. Errores típicos a revisar si un total no da
- ¿Convertí a ARS (× cotización)?
- ¿El impuesto va sobre el monto completo (no sobre el neto)?
- ¿Confundí `id` (numérico, interno) con `numero` (del plástico)?
- ¿`split(",")` en vez de `split(";")`? ¿Falta `.trim()` (rompe el parseInt por el `\r` de Windows)?
- ¿El período: filtré por anio Y mes?

## 7. Recordá
- El `data.sql` del recuperatorio trae **6 liquidaciones precargadas** (el del parcial no) →
  `getLiquidacionesPendientes(2026,5)` da 5 (no 10). Puede que pidan no duplicar (idempotencia).
- El `schema.sql` es estable: no esperes cambios de estructura.
- Objetivo final: `mvn test` en verde.
