# Ejercicio 13. Pagos y saldos por préstamo

## 1. Enunciado

Mostrar, para cada préstamo, cuánto se ha pagado y cuánto saldo queda pendiente.

Hay que usar las clases reales, no sumar pagos dos veces, no inventar métodos, respetar las fórmulas del proyecto, incluir préstamos sin pagos y explicar si hace falta una consulta por préstamo. Nada del proyecto original se modifica.

## 2. Qué conceptos evalúa

- Diferencia entre monto original, monto total con interés, total pagado y saldo pendiente.
- Qué se guarda en SQL y qué solo vive en el objeto Java.
- `PrestamoServicio` como lugar de la fórmula.
- `Collectors.groupingBy` y `summingDouble`.
- El costo de las consultas que `PrestamoDAO.mapearPrestamo` ya hace, aunque sea un método privado.

## 3. Qué archivos originales consultaste

- `src/modelo/Servicios/PrestamoServicio.java`, fórmulas reales:
  - `calcularMontoTotal`: `monto + (monto * interes / 100)`. El interés está en porcentaje. 10 significa 10, no 0,10 escrito por el usuario.
  - `calcularCuotaMensual`: `montoTotal / cuotas`. Si `cuotas == 0`, la cuota queda en 0 y no divide.
  - `inicializarSaldoPendiente`: el saldo empieza igual al monto total. Lo usa `calcularDatosPrestamo`, al crear.
  - `calcularSaldoPendiente`: suma `pago.getMonto()` con un `for`, resta esa suma al monto total, y si el resultado es negativo lo deja en 0. Si el saldo queda en 0, hace `setEstado("PAGADO")` sobre el objeto. No guarda ese estado en la base.
- `src/modelo/DAO/PrestamoDAO.java`. El `INSERT` guarda `cliente_id`, `empleado_id`, `monto`, `interes`, `cuotas`, `fecha_inicio` y `estado`. No guarda monto total ni saldo. `mapearPrestamo` es privado: después de leer la fila crea el `Prestamo` con totales en 0 y luego llama a `calcularMontoTotal`, `calcularCuotaMensual` y `calcularSaldoPendiente`. Para eso llama a `pagoDAO.obtenerPorPrestamo(prestamo.getId())`.
- `src/modelo/DAO/PagoDAO.java`. `obtenerTodos()` devuelve `List<Pago>`. `obtenerPorPrestamo(int)` devuelve los pagos de un solo crédito. `getMonto()` es `double` y `getIdPrestamo()` es `int`.
- `src/vista/MenuPago.java`. Al registrar un pago, si el monto cabe en el saldo, guarda y vuelve a calcular el saldo en memoria. No llama a `prestamoDAO.actualizar` ni a `actualizarEstado`.
- `src/SQL/Crediya.sql`. `pagos.prestamo_id` referencia `prestamos.id`. No hay columna de saldo.

No existe un método `getTotalPagado()`. El total pagado hay que obtenerlo sumando pagos.

## 4. Qué archivos nuevos creaste

- `PracticaExamenJava/Ejercicio13_PagosYSaldos/ReportePagosYSaldos.java`
- `PracticaExamenJava/Ejercicio13_PagosYSaldos/Explicacion.md`

El método nuevo es `generar`. No cambia las fórmulas: o reutiliza objetos que ya pasaron por `PrestamoServicio`, o, en el camino de base, reutiliza lo que `mapearPrestamo` ya calculó.

## 5. Solución completa

```java
public void ejecutarConBaseDeDatos() {
    PrestamoDAO prestamoDAO = new PrestamoDAO();
    PagoDAO pagoDAO = new PagoDAO();
    generar(prestamoDAO.obtenerTodos(), pagoDAO.obtenerTodos());
}

public void generar(List<Prestamo> prestamos, List<Pago> pagos) {
    System.out.println("\n--- Pagos y saldos por préstamo ---");

    if (prestamos == null || prestamos.isEmpty()) {
        System.out.println("No hay préstamos registrados.");
        return;
    }

    List<Pago> fuentePagos = pagos == null ? List.of() : pagos;

    Map<Integer, Double> totalPagadoPorPrestamo = fuentePagos.stream()
            .collect(Collectors.groupingBy(
                    Pago::getIdPrestamo,
                    Collectors.summingDouble(Pago::getMonto)
            ));

    prestamos.forEach(prestamo -> imprimirFila(prestamo, totalPagadoPorPrestamo));
}

private void imprimirFila(Prestamo prestamo, Map<Integer, Double> totalPagadoPorPrestamo) {
    double totalPagado = totalPagadoPorPrestamo.getOrDefault(prestamo.getId(), 0.0);

    System.out.printf(
            "ID: %d | Monto original: $%.2f | Monto total: $%.2f | Total pagado: $%.2f | Saldo pendiente: $%.2f | Estado: %s%n",
            prestamo.getId(),
            prestamo.getMonto(),
            prestamo.getMontoTotal(),
            totalPagado,
            prestamo.getSaldoPendiente(),
            prestamo.getEstado()
    );
}
```

`main` arma cuatro préstamos con `PrestamoServicio` y una lista de pagos. No llama a `ejecutarConBaseDeDatos`.

## 6. Explicación línea por línea de las partes importantes

1. `prestamoDAO.obtenerTodos()` devuelve préstamos cuyo `montoTotal` y `saldoPendiente` ya fueron calculados dentro de `mapearPrestamo`. Ese cálculo usa la fórmula del servicio y los pagos de cada crédito.
2. `pagoDAO.obtenerTodos()` trae cada fila de `pagos` una vez.
3. Si no hay préstamos, el reporte avisa y no intenta agrupar.
4. `pagos == null` se trata como lista vacía. El DAO real devuelve lista vacía, no `null`, cuando la consulta falla.
5. `groupingBy(Pago::getIdPrestamo, summingDouble(Pago::getMonto))` parte los pagos por el id del préstamo y suma los montos de cada grupo. El resultado es `Map<Integer, Double>`.
6. `Pago::getIdPrestamo` es el `int` que en la tabla se llama `prestamo_id`. El DAO lo guarda en el campo `idPrestamo`.
7. `getOrDefault(prestamo.getId(), 0.0)` cubre el crédito que todavía no tiene pagos. No aparece como llave del mapa y su total pagado es 0.
8. `imprimirFila` muestra cuatro ideas distintas:
   - monto original: `getMonto()`
   - monto total: `getMontoTotal()`
   - total pagado: la suma del mapa
   - saldo pendiente: `getSaldoPendiente()`
9. El saldo no se vuelve a restar. `getSaldoPendiente()` ya es `montoTotal - pagos`, con piso 0. Restar otra vez el total pagado daría un saldo falso.
10. Cada pago entra en una sola suma, la de su `idPrestamo`. No se recorre `obtenerPorPrestamo` dentro del `forEach` del reporte.

## 7. Explicación de cada método utilizado

| Método | Dónde está | Para qué se usa |
| --- | --- | --- |
| `PrestamoDAO.obtenerTodos()` | Existente, instancia | Lista de préstamos ya con monto total y saldo |
| `PagoDAO.obtenerTodos()` | Existente, instancia | Todos los pagos, una consulta |
| `PagoDAO.obtenerPorPrestamo(int)` | Existente, instancia | No se llama en el ciclo del reporte. `mapearPrestamo` ya lo usa por dentro, y volver a llamarlo duplicaría esas consultas |
| `calcularMontoTotal` | Existente | `monto + monto * interes / 100` |
| `calcularSaldoPendiente` | Existente | Resta la suma de pagos y aplica el piso 0 |
| `calcularDatosPrestamo` | Existente | En la demo, deja listo un préstamo sin pagos |
| `groupingBy` | Stream API | Agrupa pagos por préstamo |
| `summingDouble` | Stream API | Suma `getMonto()` en `double` |
| `Map.getOrDefault` | `java.util` | 0 cuando no hay pagos |

La suma manual equivalente, que es la que ya usa el servicio, es:

```java
double totalPagado = 0;
for (Pago pago : pagos) {
    totalPagado += pago.getMonto();
}
double saldoPendiente = prestamo.getMontoTotal() - totalPagado;
if (saldoPendiente < 0) {
    saldoPendiente = 0;
}
```

El reporte no copia esa resta. La deja en el servicio para no tener dos fórmulas.

### Por qué no basta con `montoTotal - saldoPendiente`

Cuando el saldo se aplasta a 0, esa resta ya no recupera lo pagado. El préstamo 4 de la demo tiene monto total 100.000, pagos por 150.000 y saldo 0. `100.000 - 0` da 100.000, no 150.000. Por eso el total pagado sale de los objetos `Pago`, no del saldo.

`MenuPago.registrarPago` impide un pago mayor que el saldo en el alta normal. `PagoDAO.actualizar` no recalcula nada. Un pago excedido puede existir si alguien modifica el monto después. El reporte no asume que eso es imposible.

### Consultas

La implementación más corta sería, por cada préstamo, llamar a `obtenerPorPrestamo`. Eso es correcto, pero repetido: `mapearPrestamo` ya hizo exactamente esa consulta al construir la lista. Serían otras N consultas iguales.

`pagoDAO.obtenerTodos()` agrega una sola consulta y agrupa en memoria. No elimina las N consultas internas de `mapearPrestamo`, porque ese método es privado y no se puede cambiar desde este laboratorio. Esas N consultas existen para que el saldo del objeto sea el del proyecto. La consulta extra de todos los pagos existe para mostrar el total pagado real, incluso si el saldo fue recortado a 0.

Un `SELECT` con `SUM(monto) GROUP BY prestamo_id` también serviría, pero no hay un método así en `PagoDAO`. Escribirlo sería un método nuevo del DAO. El enunciado pide apoyarse en lo que ya existe, así que el agrupamiento se hace en Java sobre `obtenerTodos()`.

## 8. Tipos de datos y valores de retorno

- Monto original: `double`, columna `prestamos.monto`.
- Interés: `double`, columna `prestamos.interes`, tratado como porcentaje.
- Monto total: `double`, solo en el objeto. No hay columna `monto_total`.
- Total pagado: `double` calculado. No hay columna en `prestamos`. Cada pago sí guarda su `monto` en `pagos.monto`.
- Saldo pendiente: `double`, solo en el objeto. Puede quedar 0 aunque la suma de pagos sea mayor que el monto total.
- Estado: `String`. El servicio puede dejarlo en `"PAGADO"` en memoria cuando el saldo es 0. Eso no ejecuta `UPDATE`.
- El mapa es `Map<Integer, Double>` porque `groupingBy` empaqueta el `int` del id y `summingDouble` produce `Double`.
- `getOrDefault` devuelve `double` gracias al `0.0`. Si la llave existe, devuelve la suma.

Los cuatro números del préstamo 2 de la demo:

- original 2.000.000
- total con 10 %: 2.000.000 + 200.000 = 2.200.000
- pagado: 500.000 + 200.000 = 700.000
- saldo: 2.200.000 - 700.000 = 1.500.000

El préstamo 1 no tiene pagos. Su total con 10 % es 1.100.000 y ese mismo valor queda como saldo, porque `calcularDatosPrestamo` llama a `inicializarSaldoPendiente`.

El préstamo 3 tiene interés 0. Monto original, monto total y saldo coinciden: 500.000.

## 9. Flujo de ejecución

Demo, sin base:

1. Préstamo 1, sin pagos. Saldo igual al monto total.
2. Préstamo 2, dos pagos. El servicio resta 700.000.
3. Préstamo 3, interés 0 y sin pagos.
4. Préstamo 4, pago de 150.000 contra un total de 100.000. El servicio deja saldo 0 y estado `PAGADO`. El mapa igual reporta 150.000 pagados.
5. Una lista vacía de préstamos imprime el aviso.

Salida de esa demo:

```text
ID: 1 | Monto original: $1000000,00 | Monto total: $1100000,00 | Total pagado: $0,00 | Saldo pendiente: $1100000,00 | Estado: PENDIENTE
ID: 2 | Monto original: $2000000,00 | Monto total: $2200000,00 | Total pagado: $700000,00 | Saldo pendiente: $1500000,00 | Estado: PENDIENTE
ID: 3 | Monto original: $500000,00 | Monto total: $500000,00 | Total pagado: $0,00 | Saldo pendiente: $500000,00 | Estado: PENDIENTE
ID: 4 | Monto original: $100000,00 | Monto total: $100000,00 | Total pagado: $150000,00 | Saldo pendiente: $0,00 | Estado: PAGADO
```

La coma decimal viene del idioma del sistema al usar `%.2f`, igual que en los menús del proyecto.

Camino JDBC, no ejecutado:

1. `obtenerTodos` de préstamos abre una conexión y, por cada préstamo, otra consulta de sus pagos dentro de `mapearPrestamo`.
2. `obtenerTodos` de pagos abre otra conexión y lee la tabla completa una vez.
3. `generar` solo imprime. No llama a `guardar`, `actualizar` ni `eliminar`.

## 10. Validaciones y casos límite

- Sin préstamos: mensaje y fin.
- Préstamo sin pagos: total pagado 0 y saldo igual al monto total, si el servicio ya corrió.
- Interés 0: el monto total es igual al monto original.
- Pagos que superan el monto total: el saldo mostrado es 0 y el total pagado sigue siendo la suma completa.
- Pagos cuyo `prestamo_id` no está en la lista de préstamos: quedan en el mapa y ninguna fila del reporte los usa, porque el ciclo recorre préstamos, no pagos sueltos.
- El estado `PAGADO` que pone `calcularSaldoPendiente` puede no coincidir con la columna `prestamos.estado`. Al leer, el objeto vuelve a calcularse y puede mostrar `PAGADO` aunque nadie haya hecho `UPDATE`. El reporte enseña el estado del objeto después del servicio. No lo persiste.
- Comparar el saldo con `== 0` es lo que ya hace el servicio. Este ejercicio no cambia esa comparación.
- Una lista de pagos `null` no tumba el reporte: se interpreta como “nadie ha pagado”.

## 11. Cómo se integraría conceptualmente

No está integrado. Iría en `MenuReportes` como otra opción, con los dos DAO. `MenuReportes` hoy solo tiene `PrestamoDAO` y `ClienteDAO`. Habría que declarar un `PagoDAO` en ese menú, del mismo modo que `MenuPago` ya declara el suyo. Eso sería un cambio del menú, y por eso no se hizo.

El ciclo `while (opcion != 4)` tendría que actualizarse si “Volver” cambia de número.

No se agrega una columna de saldo al SQL. El proyecto ya decidió calcularlo. Meter el saldo en la tabla sería otro diseño y dejaría dos fuentes de verdad.

Tampoco se llama a `PrestamoDAO.actualizar` desde el reporte. Un reporte no debe escribir el estado `PAGADO` como efecto de leer.

## 12. Tres preguntas que podría hacer el profesor

**¿Cuáles son los cuatro valores y dónde vive cada uno?**

El monto original está en `prestamos.monto`. El monto total es el original más el porcentaje de interés y solo está en el objeto. El total pagado es la suma de `pagos.monto` de ese `prestamo_id`. El saldo pendiente es el monto total menos esa suma, nunca menor que 0, y tampoco tiene columna.

**¿Por qué el préstamo sin pagos no muestra saldo 0?**

Porque deber no es lo mismo que haber pagado. Sin pagos, `inicializarSaldoPendiente` copia el monto total. El saldo 0 aparece cuando esa deuda ya fue cubierta, o cuando el cálculo negativo se recortó.

**¿El reporte vuelve a sumar lo que `mapearPrestamo` ya sumó?**

Vuelve a leer los pagos con una consulta distinta, `obtenerTodos`, para poder mostrar la suma. No resta esa suma del saldo otra vez. Llamar `obtenerPorPrestamo` dentro del `for` habría repetido, préstamo por préstamo, la consulta que el mapeo ya hizo. No se puede quitar esa consulta interna sin editar `PrestamoDAO`, y ese archivo no se toca.

## 13. Reto

Sin abrir la base, muestra también el nombre del cliente al lado de cada fila. Pista: arma una `List<Cliente>` en memoria y, dentro de `imprimirFila`, busca el cliente cuyo `getId()` sea igual a `prestamo.getIdCliente()`. Es el mismo emparejamiento que `MenuReportes.mostrarClientesMorosos`, pero sin anidar un `forEach` que imprima varias veces. Si no hay cliente, imprime “cliente no encontrado” y conserva los montos.

## Compilar y ejecutar la demostración

```bash
javac -encoding UTF-8 --release 21 -sourcepath src -d /tmp/crediya-practica \
  PracticaExamenJava/Ejercicio13_PagosYSaldos/ReportePagosYSaldos.java

java -cp /tmp/crediya-practica ReportePagosYSaldos
```

La clase necesita `Prestamo`, `Pago`, los dos DAO y `PrestamoServicio`. No es autónoma. `main` no usa JDBC. `ejecutarConBaseDeDatos()` leería `crediya_db`; no lo ejecutes en este repaso.
