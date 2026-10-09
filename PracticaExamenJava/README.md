# Laboratorio de práctica. CrediYa S.A.S.

Este material está aparte del programa. No se agregó al menú, no cambia los DAO y no se ejecuta desde `Main`.

Cada ejercicio tiene una clase con `main`. Ese `main` solo arma objetos en memoria y usa `PrestamoServicio` cuando hace falta la fórmula real. Los métodos que abrirían MariaDB existen para que veas el camino de examen, y ninguno de esos `main` los llama.

El proyecto compila con JDK 21. Las clases de práctica no tienen `package`: viven en el paquete por defecto para poder quedar en estas carpetas. Importan clases de `src/`, así que no son programas autónomos.

## Índice

### Ejercicio 4. Préstamo de mayor monto

- Problema: encontrar el préstamo cuyo monto original es el más alto y mostrar también su saldo.
- Conceptos: `Stream.max`, `Comparator.comparingDouble`, `Optional`, una pasada frente a ordenar toda la lista.
- Lee primero: `MenuReportes`, `PrestamoDAO.obtenerTodos`, `Prestamo.getMonto` y `Prestamo.getSaldoPendiente`.
- Archivos: `Ejercicio04_PrestamoMayorMonto/ReportePrestamoMayorMonto.java` y su `Explicacion.md`.
- En oral tienes que poder explicar qué devuelve `max` si la lista está vacía, por qué el comparador usa `getMonto()` y por qué el crédito más grande puede no ser el que más saldo debe.

### Ejercicio 7. Préstamos entre dos montos

- Problema: pedir un mínimo y un máximo, validarlos y listar los préstamos cuyo monto original cae dentro, incluidos los bordes.
- Conceptos: `filter`, la lambda y su parámetro, `forEach`, variables efectivamente finales, validar antes de consultar.
- Lee primero: `ConsolUtils.leerDouble`, `MenuReportes.mostrarPrestamosActivos` y `Prestamo.getMonto`.
- Archivos: `Ejercicio07_PrestamosPorRango/FiltroPrestamosPorRango.java` y su `Explicacion.md`.
- En oral tienes que poder explicar qué es `prestamo` dentro de la lambda, por qué el `forEach` va después de mirar si la lista filtrada quedó vacía, y por qué cero es válido aquí aunque al crear un préstamo el monto deba ser mayor que cero.

### Ejercicio 9. Los tres préstamos más grandes

- Problema: mostrar como máximo tres préstamos, de mayor a menor monto original.
- Conceptos: `sorted`, `reversed`, `limit`, `forEach`, y la diferencia entre ordenar un stream y ordenar la lista original.
- Lee primero: `PrestamoDAO.obtenerTodos` y los getters `getId`, `getMonto`, `getEstado`.
- Archivos: `Ejercicio09_TresPrestamosMayores/TresPrestamosMayores.java` y su `Explicacion.md`.
- En oral tienes que poder explicar qué pasa con una lista de uno o dos préstamos, por qué no usas `get(0)`, `get(1)` y `get(2)`, y por qué después del reporte el orden original sigue igual.

### Ejercicio 11. Documentos duplicados

- Problema: no registrar un cliente si su documento ya está guardado.
- Conceptos: método estático y método de instancia, `null` como “no existe”, validación en Java frente a `UNIQUE`, condición de carrera.
- Lee primero: `MenuCliente.registrarCliente`, `ClienteDAO.guardar` y `ClienteDAO.obtenerPorDocumento`. Mira en `Crediya.sql` que `documento` no es `UNIQUE`.
- Archivos: `Ejercicio11_DocumentosDuplicados/ValidadorDocumentoDuplicado.java` y su `Explicacion.md`.
- En oral tienes que poder explicar en qué línea del alta iría la búsqueda, por qué `guardar` se invoca con el nombre de la clase y la búsqueda con un objeto, y por qué dos usuarios simultáneos todavía podrían insertar el mismo documento.

### Ejercicio 13. Pagos y saldos

- Problema: por cada préstamo, mostrar lo pagado y lo que queda pendiente, con las fórmulas que el proyecto ya usa.
- Conceptos: monto original, monto total con interés, suma de pagos, saldo con piso 0, `groupingBy`, consultas que `mapearPrestamo` ya dispara.
- Lee primero: `PrestamoServicio`, `PrestamoDAO.mapearPrestamo`, `PagoDAO.obtenerTodos` y `PagoDAO.obtenerPorPrestamo`.
- Archivos: `Ejercicio13_PagosYSaldos/ReportePagosYSaldos.java` y su `Explicacion.md`.
- En oral tienes que poder explicar las cuatro cifras, por qué un préstamo sin pagos no tiene saldo 0, y por qué `montoTotal - saldoPendiente` no siempre es el total pagado.

### Ejercicio 14. No eliminar clientes con préstamos

- Problema: rechazar el borrado de un cliente que tenga préstamos, con un mensaje claro, y reconocer un id que no existe.
- Conceptos: foreign key sin `ON DELETE CASCADE`, el contrato real de `ClienteDAO.eliminar`, y la cadena cliente, préstamo, pago.
- Lee primero: el `CREATE TABLE` de `prestamos` y `pagos`, `ClienteDAO.eliminar`, `ClienteDAO.obtenerPorId`, `PrestamoDAO.obtenerPorIdCliente` y `PrestamoDAO.eliminar`.
- Archivos: `Ejercicio14_EliminarCliente/ProteccionEliminarCliente.java` y su `Explicacion.md`.
- En oral tienes que poder explicar por qué el `DELETE` actual no revisa hijos, por qué no sirve `obtenerPrestamosPorCliente`, y qué impide borrar un préstamo que ya tiene pagos.

## Tabla resumen

| Ejercicio | Conceptos | Clase principal relacionada | Dificultad |
| --- | --- | --- | --- |
| 4. Mayor monto | `max`, `Optional`, `Comparator` | `PrestamoDAO` / `MenuReportes` | Media |
| 7. Rango de montos | `filter`, lambda, validación | `PrestamoDAO` / `MenuReportes` | Baja |
| 9. Tres mayores | `sorted`, `limit`, lista original intacta | `PrestamoDAO` / `MenuReportes` | Baja |
| 11. Documento duplicado | DAO, `static` frente a instancia, unicidad | `ClienteDAO` / `MenuCliente` | Media |
| 13. Pagos y saldos | fórmula, relación préstamo-pago, `groupingBy` | `PrestamoServicio` / `PagoDAO` | Alta |
| 14. Eliminar cliente | foreign key, varios DAO, integridad | `ClienteDAO` / `PrestamoDAO` | Media-alta |

## Plan de estudio

Empieza por lo que solo recorre una lista de préstamos. Después pasa a reglas que cruzan tablas.

1. Ejercicio 7. Es el filtro más parecido a `mostrarPrestamosActivos`. Cuando puedas decir qué es el parámetro de la lambda y qué devuelve, sigue.
2. Ejercicio 9. Agrega orden y límite. Comprueba con la demo que la lista original no cambia y que una lista corta no revienta.
3. Ejercicio 4. Pasa de “los tres mayores” a “el mayor”. Ensaya qué es un `Optional` vacío y la diferencia entre `getMonto()` y `getSaldoPendiente()`.
4. Ejercicio 11. Aquí ya hay un DAO y una decisión antes de escribir. Lee `registrarCliente` con el archivo abierto al lado. Fíjate en qué método es estático.
5. Ejercicio 14. Intervienen dos DAO y el esquema. Dibuja clientes, préstamos y pagos antes de leer la solución. Después explica por qué el `DELETE` del cliente no basta.
6. Ejercicio 13. Déjalo al final. Junta servicio, pagos y préstamos. No memorices solo el `printf`: memoriza la fórmula y qué columnas no existen en SQL.

En cada uno, corre la demo, lee la explicación y haz el reto sin mirar la respuesta. El reto está al final de cada `Explicacion.md`.

## Cómo correr una demo

Desde la raíz del proyecto, cambia el nombre de la clase y la ruta. Ejemplo del ejercicio 4:

```bash
javac -encoding UTF-8 --release 21 -sourcepath src -d /tmp/crediya-practica \
  PracticaExamenJava/Ejercicio04_PrestamoMayorMonto/ReportePrestamoMayorMonto.java

java -cp /tmp/crediya-practica ReportePrestamoMayorMonto
```

`-sourcepath src` es obligatorio porque el compilador tiene que encontrar `modelo` y `vista`. El directorio de salida está en `/tmp` para no mezclar `.class` con el proyecto.

`%.2f` usa el separador decimal de tu sistema. Puedes ver `5000000,00` o `5000000.00`. Es el mismo número.

Estos métodos sí usarían la base real. No los llames mientras practicas:

- `ReportePrestamoMayorMonto.ejecutarConBaseDeDatos`
- `FiltroPrestamosPorRango.solicitarYFiltrarConBaseDeDatos`
- `TresPrestamosMayores.ejecutarConBaseDeDatos`
- `ValidadorDocumentoDuplicado.registrarSiDocumentoLibre` (puede hacer `INSERT`)
- `ReportePagosYSaldos.ejecutarConBaseDeDatos`
- `ProteccionEliminarCliente.ejecutarConBaseDeDatos` (puede hacer `DELETE`)

Para esos caminos harían falta MariaDB, la base del proyecto y el jar de MariaDB en el classpath. La demo no los necesita.

IntelliJ tiene como carpeta fuente `src/`. Esta práctica puede no aparecer como código del módulo. El comando de arriba es la forma prevista de ejecutarla. No hace falta mover estos archivos dentro de `src/`.
