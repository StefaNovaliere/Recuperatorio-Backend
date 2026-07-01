# 🚑 Notas de rescate para el parcial (computadoras de la facultad)

Guía rápida para destrabar los problemas típicos de arranque. El objetivo es
**llegar a que `mvn clean test` compile y corra** lo antes posible.

Versiones de este proyecto (para chequear compatibilidad):
- **Java 17** (`maven.compiler.source/target = 17` en el `pom.xml`)
- Hibernate **6.4.4.Final** · H2 **2.4.240** · JUnit **5.9.2** · Lombok **1.18.30**

Primer comando siempre:
```
mvn clean test
```
Leé el **primer** error de arriba hacia abajo (no el último). Casi siempre es uno de estos:

---

## 1) Lombok: `cannot find symbol` en getters/setters (getMonto, getNumero, etc.)
El código compila "a medias": no encuentra métodos que genera Lombok (`@Data`,
`@Getter`, etc.). Es que el IDE no está procesando las anotaciones.

**En IntelliJ:**
1. Instalar/activar el **plugin Lombok**: `Settings → Plugins → Marketplace → "Lombok"`.
2. Activar el procesamiento de anotaciones:
   `Settings → Build, Execution, Deployment → Compiler → Annotation Processors`
   → tildar **"Enable annotation processing"**.
3. Si aún falla, delegar el build a Maven:
   `Settings → Build Tools → Maven → Runner` → tildar
   **"Delegate IDE build/run actions to Maven"**.

> Si corrés con `mvn clean test` desde la terminal, Lombok anda sin tocar el IDE.
> Cuando dude, **usá Maven directo**.

---

## 2) Lombok revienta al compilar: `TypeTag :: UNKNOWN` / `class file has wrong version`
Esto es **incompatibilidad entre la versión de Lombok y el JDK** de la máquina.
Lombok 1.18.30 anda bien con JDK 17–21. Si la facultad tiene un **JDK más nuevo
(22+)**, Lombok explota.

**Opción A (recomendada) — subir Lombok** en el `pom.xml`:
```xml
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <version>1.18.34</version>   <!-- o la más nueva; soporta JDKs recientes -->
  <scope>provided</scope>
</dependency>
```
Después: `mvn clean test`.

**Opción B — forzar Java 17** si la máquina tiene varios JDK instalados:
- IntelliJ: `Settings → Build Tools → Maven → Runner → JRE` → elegir **17**.
  Y `File → Project Structure → Project → SDK` → **17**.
- Terminal: `java -version` para ver cuál toma; si hace falta, apuntar
  `JAVA_HOME` a un JDK 17.

> Regla mental: **el JDK que compila tiene que ser compatible con la versión de
> Lombok**. Si no podés cambiar el JDK, subí Lombok.

---

## 3) La base arranca vacía / errores raros al cargar `data.sql`
Síntomas: `Syntax error ... expected "ROW, (..."`, violaciones de FK, o tests que
fallan con **"La tarjeta X no existe"** aunque está en `data.sql`.

**Causa:** IntelliJ (o un "reformat") partió los `INSERT` en **varias líneas**.
El importador por defecto de Hibernate lee el script **línea por línea**, así que
un INSERT multilínea se rompe.

**Fix rápido (el más seguro):** dejar **cada `INSERT` en UNA sola línea** en
`src/main/resources/data.sql`. No lo reformatees.

**Fix robusto (para que no importe cómo quede):** agregar esta propiedad dentro
de `<properties>` en `src/main/resources/META-INF/persistence.xml`:
```xml
<property name="hibernate.hbm2ddl.import_files_sql_extractor"
          value="org.hibernate.tool.schema.internal.script.MultiLineSqlScriptExtractor"/>
```
Ese extractor parsea el script por `;` en vez de por línea → los INSERT multilínea
funcionan. Después: `mvn clean test`.

---

## 4) Archivos `.csv` (para `liquidarLote`)
- El CSV va en `src/main/resources/` (ej. `liquidaciones.csv`). Maven lo copia a
  `target/classes/` al compilar.
- En el **test** se ubica así (por classpath, no por ruta absoluta):
  ```java
  URL url = getClass().getClassLoader().getResource("liquidaciones.csv");
  liquidacionService.liquidarLote(url.getPath());
  ```
  Para apuntar a otro archivo, cambiá **solo** el nombre en `getResource("...")`.
- **Windows:** `url.getPath()` puede venir como `/C:/...`. Por eso el service
  corrige la ruta:
  ```java
  if (rutaArchivo.matches("/[A-Za-z]:.*")) rutaArchivo = rutaArchivo.substring(1);
  ```
- **Separador:** si el CSV usa otro separador, cambiá el `split` en el service:
  - coma → `linea.split(",")`
  - pipe → `linea.split("\\|")`  ← el pipe es regex, **va escapado**
  - con encabezado → `.filter(l -> !l.startsWith("id"))` para saltear la 1ª línea

---

## 5) Recordatorios de JPQL (errores que cuestan puntos)
- Parámetros: `WHERE c.anio = :anio` ✅  — **no** `=: anio` ❌ (no bindea).
- Atributo directo vs relación: `c.moneda` (columna) vs `c.tarjeta.numero`
  (navega la relación `@ManyToOne`). Si escribís `c.consumo.moneda` → error de path.
- `SUM(...)` sobre un período **vacío devuelve `null`**, no 0. Siempre:
  ```java
  Double total = ...getSingleResult();
  return (total == null) ? 0.0 : total;
  ```
- `COUNT(...)` devuelve **`Long`** → tipar `createQuery(jpql, Long.class)`.
- `SELECT DISTINCT c.moneda` devuelve **`String`** → tipar `String.class`
  (no `Consumo.class`).

---

## 6) Checklist de arranque en la máquina de la facultad
1. `java -version` y `mvn -version` → confirmar que hay JDK y Maven.
2. Abrir el proyecto, esperar que Maven baje dependencias.
3. `mvn clean test` desde la terminal.
4. Si falla por Lombok → sección 1 o 2.
5. Si falla por `data.sql` → sección 3.
6. Si compila pero un test da número raro → revisar reglas/JPQL (secciones 4 y 5).
7. Ante la duda, **correr por terminal con Maven**, no por el botón del IDE.
