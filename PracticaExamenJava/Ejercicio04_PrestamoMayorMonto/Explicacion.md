# Ejercicio 4. Préstamo de mayor monto

## 1. Enunciado

Crear un reporte que encuentre y muestre el préstamo con el monto más alto registrado.

Debe obtener los préstamos con métodos que ya existen, usar Stream API, avisar si no hay préstamos y mostrar id, monto, estado y saldo pendiente. Hay que distinguir el mayor monto original del mayor saldo pendiente. `MenuReportes.java` no se modifica.

## 2. Qué conceptos evalúa

- `Stream.max` y `Comparator.comparingDouble`.
- `Optional`: qué ocurre si el stream está vacío.
- Referencias a método (`Prestamo::getMonto`).
- Diferencia entre un atributo persistido (`monto`) y uno calculado en memoria (`saldoPendiente`).
- Método de instancia: `new PrestamoDAO()` y después `obtenerTodos()`.

## 3. Qué archivos originales consultaste

- `src/vista/MenuReportes.java`. Ya recorre préstamos con `prestamoDAO.obtenerTodos()` y `stream()`. El campo `prestamoDAO` es de instancia, guardado en un atributo `static final`.
- `src/modelo/DAO/PrestamoDAO.java`. `obtenerTodos()` devuelve `List<Prestamo>`. No existe un método “préstamo de mayor monto”. `mapearPrestamo` es privado y, al armar cada objeto, llama a `PrestamoServicio` y a `PagoDAO.obtenerPorPrestamo`.
- `src/modelo/Clases/Prestamo.java`. `getMonto()` es el monto original. `getSaldoPendiente()` es otro campo. El constructor corto deja el estado en `"PENDIENTE"`.
- `src/modelo/Servicios/PrestamoServicio.java`. El saldo no sale de una columna SQL.
- `src/SQL/Crediya.sql`. La tabla `prestamos` guarda `monto`, no `saldo_pendiente`.

## 4. Qué archivos nuevos creaste

- `PracticaExamenJava/Ejercicio04_PrestamoMayorMonto/ReportePrestamoMayorMonto.java`
- `PracticaExamenJava/Ejercicio04_PrestamoMayorMonto/Explicacion.md`

No hay carpeta `fragmentos/`: la integración conceptual cabe en un solo método de `MenuReportes`.

## 5. Solución completa

La clase compilable es `ReportePrestamoMayorMonto.java`. No forma parte del programa. `main` no abre MariaDB.

Esta es la parte que responde el enunciado. `mostrarPrestamoDeMayorSaldo` está solo para comparar las dos preguntas.

```java
public void ejecutarConBaseDeDatos() {
    PrestamoDAO prestamoDAO = new PrestamoDAO();
    List<Prestamo> prestamos = prestamoDAO.obtenerTodos();
    mostrarPrestamoDeMayorMonto(prestamos);
}

public void mostrarPrestamoDeMayorMonto(List<Prestamo> prestamos) {
    System.out.println("\n--- Préstamo de mayor monto original ---");

    List<Prestamo> fuente = prestamos == null ? List.of() : prestamos;

    Optional<Prestamo> mayor = fuente.stream()
            .max(Comparator.comparingDouble(Prestamo::getMonto));

    mayor.ifPresentOrElse(
            this::imprimirPrestamo,
            () -> System.out.println("No hay préstamos registrados.")
    );
}

private void imprimirPrestamo(Prestamo prestamo) {
    System.out.printf(
            "ID: %d | Monto: $%.2f | Estado: %s | Saldo pendiente: $%.2f%n",
            prestamo.getId(),
            prestamo.getMonto(),
            prestamo.getEstado(),
            prestamo.getSaldoPendiente()
    );
}
```

`ejecutarConBaseDeDatos` es el camino con JDBC. No está llamado desde `main`.

## 6. Explicación línea por línea de las partes importantes

1. `PrestamoDAO prestamoDAO = new PrestamoDAO();` crea el DAO. `obtenerTodos` no es estático. `ClienteDAO.guardar` sí lo es; este método no.
2. `prestamoDAO.obtenerTodos()` ejecuta `SELECT` de `prestamos` y devuelve la lista. Si la consulta falla, el DAO real imprime el error y devuelve una lista vacía, no `null`.
3. `fuente = prestamos == null ? List.of() : prestamos` evita un `NullPointerException` si alguien pasa `null`. Con el DAO real ese caso no ocurre.
4. `fuente.stream()` abre un flujo sobre la lista. No copia los préstamos y no reordena la lista.
5. `Comparator.comparingDouble(Prestamo::getMonto)` compara el `double` devuelto por `getMonto`. La referencia `Prestamo::getMonto` equivale a `prestamo -> prestamo.getMonto()`.
6. `max(...)` recorre el stream una sola vez y se queda con el mayor según ese comparador. El resultado es un `Optional<Prestamo>`, nunca un `Prestamo` directo.
7. Si la lista está vacía, el `Optional` está vacío. `ifPresentOrElse` ejecuta la segunda lambda y no llama a `imprimirPrestamo`.
8. Si hay un ganador, `ifPresentOrElse` entrega ese objeto a `imprimirPrestamo`. El parámetro de esa lambda es el préstamo elegido.
9. `printf` muestra `getId()`, `getMonto()`, `getEstado()` y `getSaldoPendiente()`. El saldo se imprime para cumplir el enunciado, pero no participa en la comparación.
10. `%.2f` usa el separador decimal del idioma del sistema. En esta máquina se vio `5000000,00`. En otro equipo puede verse `5000000.00`. El valor es el mismo.

## 7. Explicación de cada método utilizado

| Método | De quién es | Qué hace | Qué devuelve |
| --- | --- | --- | --- |
| `PrestamoDAO.obtenerTodos()` | Existente, instancia | Lee todos los préstamos | `List<Prestamo>` |
| `Stream.max(Comparator)` | Stream API | Elige el máximo | `Optional<Prestamo>` |
| `Comparator.comparingDouble` | `java.util` | Arma un comparador de `double` | `Comparator<Prestamo>` |
| `Prestamo::getMonto` | Existente | Lee el monto original | `double` |
| `Optional.ifPresentOrElse` | `java.util` | Una acción si hay valor y otra si no | `void` |
| `PrestamoServicio.calcularDatosPrestamo` | Existente | Llena monto total, cuota y saldo inicial | `void` |
| `PrestamoServicio.calcularSaldoPendiente` | Existente | Resta los pagos al monto total | `double` |

`mostrarPrestamoDeMayorSaldo` es nuevo y pedagógico. Cambia únicamente la referencia a `Prestamo::getSaldoPendiente`.

Conviene usar `max` y no `sorted` + `findFirst`: `max` hace una pasada. Ordenar toda la lista para quedarse con un elemento trabaja de más. Las dos formas pueden dar el mismo préstamo.

Si dos préstamos tienen el mismo `getMonto()`, el stream ordenado de una lista se queda con el que aparece primero. La reducción usa “el primero gana el empate”.

## 8. Tipos de datos y valores de retorno

- `List<Prestamo>`: cero o más préstamos. Vacía si no hay filas o si el DAO atrapó un `SQLException`.
- `Optional<Prestamo>`: o contiene el préstamo ganador, o está vacío. No uses `get()` sin preguntar antes: en un `Optional` vacío lanza `NoSuchElementException`.
- `getMonto()` devuelve `double`. Es la columna `prestamos.monto`.
- `getSaldoPendiente()` devuelve `double`. No es una columna. Después de `obtenerTodos()`, `mapearPrestamo` ya lo calculó.
- `getEstado()` devuelve `String`: `"PENDIENTE"`, `"PAGADO"` o `"CANCELADO"` en el uso normal del proyecto.
- `getId()` devuelve `int`.

## 9. Flujo de ejecución

Demostración en memoria, que es la que ejecuta `main`:

1. Crea tres préstamos con el constructor real `Prestamo(idCliente, idEmpleado, monto, interes, cuotas)`.
2. `calcularDatosPrestamo` aplica la fórmula del proyecto.
3. Al préstamo 2, de monto 5.000.000 e interés 10, se le resta un pago de 5.000.000. Su monto total queda en 5.500.000 y su saldo en 500.000.
4. El préstamo 1 tiene monto 2.000.000, sin pagos. Su saldo queda en 2.200.000.
5. `mostrarPrestamoDeMayorMonto` elige el id 2.
6. `mostrarPrestamoDeMayorSaldo` elige el id 1.
7. Una lista vacía imprime “No hay préstamos registrados.”.

Salida real de esa demostración:

```text
--- Préstamo de mayor monto original ---
ID: 2 | Monto: $5000000,00 | Estado: PENDIENTE | Saldo pendiente: $500000,00

--- Contraste: préstamo de mayor saldo pendiente ---
ID: 1 | Monto: $2000000,00 | Estado: PENDIENTE | Saldo pendiente: $2200000,00

--- Préstamo de mayor monto original ---
No hay préstamos registrados.
```

Camino con base de datos, no ejecutado aquí:

1. `new PrestamoDAO()`.
2. `obtenerTodos()` abre conexión con `ConexionBD.obtenerConexion()`.
3. Por cada fila, `mapearPrestamo` consulta los pagos de ese préstamo y calcula el saldo.
4. El mismo `max` elige el mayor `getMonto()`.

## 10. Validaciones y casos límite

- Lista vacía: el `Optional` queda vacío y hay mensaje. No se llama a `imprimirPrestamo`.
- Lista `null`: se trata como vacía. El DAO real no devuelve `null`.
- Un solo préstamo: ese es el máximo.
- Empate de monto: se conserva el primero del recorrido.
- El mayor monto puede tener el menor saldo, como el id 2 de la demo. Por eso el comparador debe nombrar `getMonto` y no `getSaldoPendiente`.
- `mapearPrestamo`, si el saldo calculado es 0, pone el estado del objeto en `"PAGADO"`. Eso ocurre en memoria al leer. El reporte de este ejercicio no cambia esa regla.

## 11. Cómo se integraría conceptualmente

No está integrado. En el examen, si permiten editar el menú, el sitio natural es `MenuReportes`.

Hoy las opciones llegan hasta 4 y el ciclo dice `while (opcion != 4)`. 4 es “Volver”. Si insertas el reporte como opción 4 y pasas “Volver” a 5, también tienes que cambiar esa condición. Si no la cambias, el programa sale al elegir el reporte nuevo.

El atributo `prestamoDAO` ya existe en `MenuReportes`. No haría falta otro DAO. El cuerpo sería el de `mostrarPrestamoDeMayorMonto`, llamando a `prestamoDAO.obtenerTodos()`.

No hace falta tocar `Prestamo`, `PrestamoDAO` ni el SQL: el monto ya viene en cada objeto.

## 12. Tres preguntas que podría hacer el profesor

**¿Qué devuelve `max` si no hay préstamos?**

Devuelve `Optional.empty()`. No devuelve `null` y no lanza excepción por estar vacío. La excepción aparece solo si llamas `get()` sobre ese `Optional` vacío.

**¿Por qué el préstamo de mayor monto no es el de mayor saldo?**

`getMonto()` es el capital guardado en `prestamos.monto`. `getSaldoPendiente()` es `montoTotal - pagos`, con el monto total ya aumentado por el interés y con un piso de 0. Un crédito grande casi pagado tiene saldo bajo. Un crédito menor, sin pagos, puede deber más en ese momento.

**¿`sorted().findFirst()` es lo mismo?**

Puede mostrar el mismo préstamo, pero ordena todos los elementos. `max` solo necesita recordar el mayor mientras recorre. Además `findFirst()` también devuelve `Optional`.

## 13. Reto

Cambia el comparador para que, si dos préstamos tienen el mismo monto, gane el de menor id. Pista: `Comparator.comparingDouble(Prestamo::getMonto).thenComparingInt(Prestamo::getId)`. Prueba el empate con dos préstamos de 5.000.000 y ids 20 y 4. No hace falta base de datos: agrega los objetos en `crearDatosDePractica`.

## Compilar y ejecutar la demostración

Desde la raíz del proyecto. Estas clases no tienen `package` y dependen de `src/`. No son autónomas. Esta corrida no necesita el controlador JDBC porque `main` no conecta.

```bash
javac -encoding UTF-8 --release 21 -sourcepath src -d /tmp/crediya-practica \
  PracticaExamenJava/Ejercicio04_PrestamoMayorMonto/ReportePrestamoMayorMonto.java

java -cp /tmp/crediya-practica ReportePrestamoMayorMonto
```

`ejecutarConBaseDeDatos()` sí necesita MariaDB, la base `crediya_db` y el jar de MariaDB en el classpath. No lo ejecutes mientras repasas: leería la base real.
