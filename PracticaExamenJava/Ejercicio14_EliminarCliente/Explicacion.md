# Ejercicio 14. Impedir eliminar clientes con préstamos

## 1. Enunciado

Impedir que se elimine un cliente cuando tiene préstamos asociados.

Hay que revisar las relaciones reales, revisar `ClienteDAO.eliminar(int)`, consultar los préstamos antes de borrar, explicar el rechazo, atender al cliente que no existe y tener en cuenta que esos préstamos pueden tener pagos. No se modifica ningún DAO, menú ni archivo SQL.

## 2. Qué conceptos evalúa

- Integridad referencial y claves foráneas.
- Diferencia entre un `DELETE` que afecta 0 filas y un `DELETE` que la base rechaza.
- Reutilizar métodos de instancia de dos DAO.
- No usar un método existente solo porque “habla de préstamos”, si su contrato no sirve para decidir.
- Orden correcto de las comprobaciones: primero el cliente, después sus préstamos.

## 3. Qué archivos originales consultaste

- `src/SQL/Crediya.sql`:
  - `prestamos.cliente_id` es `INT NOT NULL` y tiene `FOREIGN KEY (cliente_id) REFERENCES clientes(id)`.
  - `prestamos.empleado_id` referencia `empleados(id)`.
  - `pagos.prestamo_id` referencia `prestamos(id)`.
  - Ninguna clave declara `ON DELETE CASCADE`. En InnoDB, si no escribes la acción, el borrado de un padre con hijos se rechaza.
- `src/modelo/DAO/ClienteDAO.java`, método `eliminar(int id)`. El SQL es solo `DELETE FROM clientes WHERE id = ?`. No consulta `prestamos`. Devuelve `true` si `executeUpdate()` es mayor que 0. Si MariaDB lanza `SQLException`, imprime “Error al eliminar cliente” y devuelve `false`.
- `src/modelo/DAO/ClienteDAO.java`, `obtenerPorId(int)`. Devuelve `Cliente` o `null`.
- `src/modelo/DAO/ClienteDAO.java`, `obtenerPrestamosPorCliente(String documento)`. Devuelve `void`, imprime por consola y usa `INNER JOIN`. Si el cliente no existe o si existe pero no tiene préstamos, el mensaje es el mismo: “no tiene préstamos asociados o no existe”. No sirve para decidir un borrado.
- `src/modelo/DAO/PrestamoDAO.java`, `obtenerPorIdCliente(int)`. Devuelve `List<Prestamo>`. Lista vacía si ese id no tiene créditos. Este sí sirve.
- `src/modelo/DAO/PrestamoDAO.java`, `eliminar(int)`. Este sí revisa hijos, pero los hijos del préstamo: si `pagoDAO.obtenerPorPrestamo(id)` no está vacío, no borra el préstamo. No protege el borrado del cliente.
- `src/vista/MenuCliente.java`. No hay opción de eliminar cliente. El módulo solo registra, lista y consulta préstamos por documento.

## 4. Qué archivos nuevos creaste

- `PracticaExamenJava/Ejercicio14_EliminarCliente/ProteccionEliminarCliente.java`
- `PracticaExamenJava/Ejercicio14_EliminarCliente/Explicacion.md`

Reutilizado tal cual:

- `ClienteDAO.obtenerPorId(int)`
- `PrestamoDAO.obtenerPorIdCliente(int)`
- `ClienteDAO.eliminar(int)`, únicamente después de la validación nueva

Nuevo:

- `decidirSiSePuedeEliminar(Cliente, List<Prestamo>)`
- `ejecutarConBaseDeDatos(int)`, que orquesta los tres métodos existentes

No se creó un SQL nuevo ni un método dentro de `ClienteDAO`.

## 5. Solución completa

La decisión, que no borra nada:

```java
public boolean decidirSiSePuedeEliminar(Cliente cliente, List<Prestamo> prestamos) {
    if (cliente == null) {
        System.out.println("No existe un cliente con el ID indicado.");
        return false;
    }

    if (prestamos != null && !prestamos.isEmpty()) {
        String ids = prestamos.stream()
                .map(prestamo -> String.valueOf(prestamo.getId()))
                .collect(Collectors.joining(", "));

        System.out.printf(
                "No se puede eliminar al cliente %s (ID %d) porque tiene %d préstamo(s) asociado(s): %s.%n",
                cliente.getNombre(),
                cliente.getId(),
                prestamos.size(),
                ids
        );
        System.out.println("Esos préstamos pueden tener pagos. La clave foránea de prestamos.cliente_id impide borrar el cliente, y la de pagos.prestamo_id impide borrar un préstamo que aún tenga pagos.");
        return false;
    }

    System.out.println("El cliente " + cliente.getNombre() + " no tiene préstamos. La eliminación estaría permitida.");
    return true;
}
```

La orquestación con la base, no llamada desde `main`:

```java
public boolean ejecutarConBaseDeDatos(int idCliente) {
    ClienteDAO clienteDAO = new ClienteDAO();
    PrestamoDAO prestamoDAO = new PrestamoDAO();

    Cliente cliente = clienteDAO.obtenerPorId(idCliente);
    List<Prestamo> prestamos = List.of();
    if (cliente != null) {
        prestamos = prestamoDAO.obtenerPorIdCliente(idCliente);
    }

    if (!decidirSiSePuedeEliminar(cliente, prestamos)) {
        return false;
    }

    boolean eliminado = clienteDAO.eliminar(idCliente);
    if (eliminado) {
        System.out.println("Cliente eliminado correctamente.");
    } else {
        System.out.println("No se pudo eliminar el cliente.");
    }
    return eliminado;
}
```

`main` solo llama a `decidirSiSePuedeEliminar` con objetos armados en memoria. No llama a `eliminar`.

## 6. Explicación línea por línea de las partes importantes

1. `obtenerPorId` es la primera consulta. Si devuelve `null`, el id no está en `clientes`.
2. Solo si el cliente existe se llama a `obtenerPorIdCliente`. Buscar préstamos de un id inexistente devolvería lista vacía, y una lista vacía significa “se podría borrar”. Sin el primer `if`, un id fantasma parecería un cliente borrable.
3. `decidirSiSePuedeEliminar` repite la pregunta del `null` para que la demo, que no tiene DAO, también distinga “no existe”.
4. `prestamos != null && !prestamos.isEmpty()` es el bloqueo. Con un solo préstamo ya no se puede borrar al cliente.
5. El `stream` no decide la regla. Solo convierte los ids a texto para el mensaje: `10, 11`. `map` recibe cada `Prestamo` y `joining` concatena.
6. El `return false` de ese bloque ocurre antes de `clienteDAO.eliminar`. El `DELETE` no sale.
7. El mensaje nombra las dos relaciones. No consulta `pagos`, porque no hace falta para rechazar: la existencia del préstamo alcanza. Los pagos importan si alguien intentara borrar primero los préstamos para poder borrar después al cliente.
8. Si la lista de préstamos está vacía, el método devuelve `true`. En la demo eso solo imprime que la eliminación estaría permitida. En `ejecutarConBaseDeDatos`, ese `true` sí continúa hacia `eliminar`.
9. `eliminar` sigue siendo el método original. Devuelve `true` solo cuando la base borra una fila.

## 7. Explicación de cada método utilizado

| Método | ¿Existe? | Qué aporta |
| --- | --- | --- |
| `ClienteDAO.obtenerPorId(int)` | Sí, instancia | `Cliente` o `null` |
| `PrestamoDAO.obtenerPorIdCliente(int)` | Sí, instancia | `List<Prestamo>` de ese `cliente_id` |
| `ClienteDAO.eliminar(int)` | Sí, instancia, sin validar hijos | `boolean` del `DELETE` |
| `ClienteDAO.obtenerPrestamosPorCliente(String)` | Sí, pero no se usa | `void`. No deja decidir y mezcla “no existe” con “no tiene préstamos” |
| `PrestamoDAO.eliminar(int)` | Sí, no se llama | Muestra que el proyecto ya supo proteger un padre, el préstamo, frente a sus pagos. El cliente no tiene esa protección |
| `decidirSiSePuedeEliminar` | Nuevo | `boolean`. No escribe en la base |
| `ejecutarConBaseDeDatos` | Nuevo | Junta la decisión con el `DELETE` real |

`obtenerPorIdCliente`, si el `SELECT` lanza `SQLException`, imprime el error y devuelve lista vacía. En ese fallo, la orquestación podría creer que no hay préstamos. Es una limitación del DAO existente. No se corrige aquí. En condiciones normales, la lista vacía significa que el `SELECT` respondió y no hubo filas.

## 8. Tipos de datos y valores de retorno

- El id del cliente es `int`, igual que `clientes.id` y `prestamos.cliente_id`.
- `obtenerPorId` devuelve referencia o `null`. No devuelve `Optional`.
- `obtenerPorIdCliente` devuelve lista. Vacía no es `null`.
- `decidirSiSePuedeEliminar` devuelve `false` por cliente ausente y también por cliente con préstamos. El mensaje permite distinguir los dos `false`.
- `eliminar` devuelve `false` en dos situaciones distintas del JDBC actual:
  - el `DELETE` afectó 0 filas, por ejemplo porque el id ya no está;
  - hubo `SQLException`, por ejemplo la clave foránea, y el `catch` devolvió `false`.
- El llamador que solo mira el `boolean` de `eliminar` no sabe cuál de las dos pasó. La validación nueva separa “no existe” y “tiene préstamos” antes de llegar ahí.

## 9. Flujo de ejecución

Relación real:

```text
clientes (id)
    ^
    | cliente_id  NOT NULL, sin ON DELETE CASCADE
prestamos (id)
    ^
    | prestamo_id NOT NULL, sin ON DELETE CASCADE
pagos (id)
```

Un cliente puede tener varios préstamos. Un préstamo puede tener varios pagos. Borrar al cliente con préstamos lo impide la foreign key de `prestamos`. Borrar uno de esos préstamos mientras tenga pagos lo impide la foreign key de `pagos` y, además, el `if` que ya escribió `PrestamoDAO.eliminar`.

Este ejercicio no borra pagos ni préstamos para “liberar” al cliente. Solo niega el `DELETE` del cliente.

Demo en memoria:

1. Ana, id 1, préstamos 10 y 11. Resultado: no se puede eliminar. Se listan los dos ids.
2. Luis, id 2, lista vacía. Resultado: la eliminación estaría permitida. No se llama a `eliminar`.
3. Cliente `null`. Resultado: no existe.

Camino JDBC, no ejecutado, para un id real:

1. `obtenerPorId`.
2. Si es `null`, mensaje y `false`. Cero `DELETE`.
3. `obtenerPorIdCliente`.
4. Si la lista tiene elementos, mensaje y `false`. Cero `DELETE`.
5. Si no tiene, `clienteDAO.eliminar(id)`.

## 10. Validaciones y casos límite

- Cliente inexistente: mensaje propio. No se interpreta la lista vacía como permiso.
- Cliente con un préstamo: se bloquea aunque ese préstamo no tenga pagos. La foreign key no pregunta si hay pagos; le basta la fila de `prestamos`.
- Cliente con varios préstamos: el mensaje dice la cantidad y los ids.
- Cliente con préstamos que a su vez tienen pagos: también se bloquea. Para llegar a borrar al cliente habría que borrar antes los pagos, después los préstamos y solo entonces el cliente. Esa cascada manual no forma parte de la solución. `PrestamoDAO.eliminar` rechazaría el paso intermedio mientras existan pagos.
- Cliente sin préstamos: la validación permite seguir. En la demo no se borra. En el método de base sí se llamaría a `eliminar`.
- Lista de préstamos `null`: se trata como “no hay préstamos”, igual que el cuidado con listas nulas de los otros ejercicios. El DAO real no devuelve `null`.
- Carrera breve: el cliente puede recibir un préstamo entre el `SELECT` y el `DELETE`. Entonces `eliminar` cae en el `catch` de la foreign key y devuelve `false`. El mensaje sería el genérico del DAO, no el de esta validación. Es el mismo tipo de hueco que en el documento duplicado: la comprobación Java no sustituye a la restricción. Aquí la restricción sí existe, y es la foreign key. La validación existe para explicar el motivo antes de provocar el error SQL.
- Borrar un empleado con préstamos chocaría con `prestamos.empleado_id` por la misma razón. `EmpleadoDAO.eliminar` tampoco revisa hijos. No es el ejercicio, pero la relación está en el mismo `CREATE TABLE`.

## 11. Cómo se integraría conceptualmente

No está integrado. `MenuCliente` no tiene “Eliminar cliente”. Habría que agregar una opción. Hoy el ciclo sale con `while (opcion != 4)` y 4 es volver. Una opción 4 nueva obligaría a mover “Volver” y a cambiar esa condición.

El `case` pediría el id con `ConsolUtils.leerEntero` y llamaría a `ejecutarConBaseDeDatos(id)`. No pegaría el `DELETE` directo de `clienteDAO.eliminar`, porque ese método sigue sin mirar préstamos.

No se modifica `ClienteDAO.eliminar`. Si el profesor pide cambiar el DAO en el examen, el lugar sería al inicio de `eliminar`, antes del `DELETE`, llamando a un `PrestamoDAO` y devolviendo `false` si la lista no está vacía. Eso sí editaría un archivo original. Esta práctica lo deja en una clase aparte para no arriesgar el proyecto, y la explicación marca esa diferencia.

Tampoco se agrega `ON DELETE CASCADE`. Cascada borraría préstamos al borrar el cliente, que es lo contrario de “impedir la eliminación”. Y aunque se borraran los préstamos, los pagos seguirían bloqueando el borrado del préstamo, salvo que la cascada también estuviera en `pagos`.

## 12. Tres preguntas que podría hacer el profesor

**¿`ClienteDAO.eliminar` ya comprueba los préstamos?**

No. El método solo ejecuta `DELETE FROM clientes WHERE id = ?`. Si hay préstamos, quien responde es MariaDB con la foreign key, y el `catch` convierte eso en `false` y en un mensaje de error SQL. La validación clara es nueva y está fuera del DAO.

**¿Por qué no usas `obtenerPrestamosPorCliente`?**

Porque devuelve `void`, pide documento en lugar de id, imprime como efecto secundario y no distingue un cliente inexistente de un cliente sin créditos. `PrestamoDAO.obtenerPorIdCliente` devuelve la lista y deja decidir.

**¿Qué papel tienen los pagos si el bloqueo ocurre por los préstamos?**

Impiden el atajo de borrar los préstamos y luego al cliente. `pagos.prestamo_id` apunta a `prestamos.id` sin cascada. `PrestamoDAO.eliminar` además lo consulta y se niega si hay pagos. Por eso un cliente con créditos que ya recibieron abonos tiene dos niveles de hijos. La regla de este ejercicio se detiene en el primer nivel: si hay préstamos, el cliente se queda.

## 13. Reto

Amplía la demo, sin base de datos, para que el mensaje diga cuántos de esos préstamos tienen pagos. Pasa una `List<Pago>` aparte y cuenta los pagos cuyo `getIdPrestamo()` esté entre los ids de los préstamos del cliente. No llames a `eliminar`. Comprueba tres casos: préstamos sin pagos, préstamos con pagos y cliente sin préstamos. El tercer caso debe seguir permitido. Los dos primeros deben seguir bloqueados; solo cambia el texto.

## Compilar y ejecutar la demostración

```bash
javac -encoding UTF-8 --release 21 -sourcepath src -d /tmp/crediya-practica \
  PracticaExamenJava/Ejercicio14_EliminarCliente/ProteccionEliminarCliente.java

java -cp /tmp/crediya-practica ProteccionEliminarCliente
```

La clase importa `Cliente`, `Prestamo`, `ClienteDAO` y `PrestamoDAO`. No es autónoma. `main` no borra nada. `ejecutarConBaseDeDatos(int)` sí puede ejecutar `DELETE FROM clientes` cuando el cliente existe y no tiene préstamos. No lo llames contra tu MariaDB de CrediYa.
