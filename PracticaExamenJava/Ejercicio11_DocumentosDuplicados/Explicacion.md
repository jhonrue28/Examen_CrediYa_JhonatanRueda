# Ejercicio 11. Documentos duplicados

## 1. Enunciado

Impedir que se registre un cliente con un documento que ya exista.

Hay que mirar cómo se crea un cliente hoy, reutilizar la búsqueda por documento si sirve, marcar como nuevo lo que no exista, decir en qué punto del alta va la validación, mostrar un mensaje claro y no insertar el duplicado. También hay que distinguir la validación en Java de una restricción `UNIQUE` en la base. El SQL original no se cambia.

## 2. Qué conceptos evalúa

- Flujo de alta en la capa de vista, antes del `INSERT`.
- Método estático frente a método de instancia dentro de la misma clase DAO.
- `null` como “no encontrado”.
- Unicidad en aplicación y unicidad en base de datos.
- Condición de carrera del patrón “consultar y después insertar”.

## 3. Qué archivos originales consultaste

- `src/vista/MenuCliente.java`, método `registrarCliente`. Lee nombre, documento, correo y teléfono con `ConsolUtils`. Construye `new Cliente(nombre, documento, correo, telefono)` y llama a `ClienteDAO.guardar(nuevoCliente)`. No busca el documento antes.
- `src/modelo/DAO/ClienteDAO.java`:
  - `public static boolean guardar(Cliente cliente)` inserta y, si hay llave generada, hace `cliente.setId`. No pregunta si el documento existe.
  - `public Cliente obtenerPorDocumento(String documento)` es de instancia. El SQL es `SELECT ... FROM clientes WHERE documento = ?`. Si hay fila, devuelve el `Cliente`. Si no hay fila, devuelve `null`.
- `src/modelo/Clases/Cliente.java`. Hay dos constructores. El de alta no recibe id. El documento se hereda de `Persona` y se lee con `getDocumento()`.
- `src/vista/ConsolUtils.java`. `leerTexto` hace `trim()`.
- `src/SQL/Crediya.sql`. En `clientes`, `documento` es `VARCHAR(30) NOT NULL`. No hay `UNIQUE (documento)`. Lo mismo pasa con `empleados.documento`.

No existe un método `existeDocumento`. No lo inventé como si ya estuviera. La búsqueda existente alcanza: si `obtenerPorDocumento` no devuelve `null`, el documento está ocupado.

## 4. Qué archivos nuevos creaste

- `PracticaExamenJava/Ejercicio11_DocumentosDuplicados/ValidadorDocumentoDuplicado.java`
- `PracticaExamenJava/Ejercicio11_DocumentosDuplicados/Explicacion.md`

Método nuevo: `registrarSiDocumentoLibre`. La demo equivalente es `registrarEnMemoria`. Las dos deciden antes de agregar. Solo la primera llamaría a los métodos reales del DAO, y `main` no la llama.

## 5. Solución completa

```java
public boolean registrarSiDocumentoLibre(Cliente nuevo) {
    if (nuevo == null || nuevo.getDocumento() == null || nuevo.getDocumento().isBlank()) {
        System.out.println("El documento es obligatorio.");
        return false;
    }

    ClienteDAO clienteDAO = new ClienteDAO();
    Cliente existente = clienteDAO.obtenerPorDocumento(nuevo.getDocumento());

    if (existente != null) {
        System.out.printf(
                "No se puede registrar el cliente. Ya existe un cliente con el documento %s (ID %d, nombre %s).%n",
                existente.getDocumento(),
                existente.getId(),
                existente.getNombre()
        );
        return false;
    }

    return ClienteDAO.guardar(nuevo);
}
```

Ese método es el que usaría la base. `return false` ocurre antes de `guardar`, así que el duplicado no se inserta.

`main` solo llama a `registrarEnMemoria`, que aplica la misma decisión sobre una `ArrayList` y no abre MariaDB.

## 6. Explicación línea por línea de las partes importantes

1. El primer `if` cubre cliente nulo, documento nulo y documento en blanco. `isBlank()` también considera espacios. `leerTexto` ya recorta, pero el método nuevo no debe confiar en que toda llamada pasó por el menú.
2. `new ClienteDAO()` hace falta porque `obtenerPorDocumento` no es estático. Llamar `ClienteDAO.obtenerPorDocumento(...)` no compila con el código actual.
3. `obtenerPorDocumento` ejecuta el `SELECT` con `PreparedStatement` y `setString`. Una fila produce un `Cliente` mapeado con el constructor de cinco argumentos, incluido el id.
4. `existente != null` significa “ya hay al menos un cliente con ese documento”. El DAO hace un solo `rs.next()`, así que devuelve el primero si ya hubiera duplicados viejos.
5. El `printf` usa el id y el nombre de esa fila para que el mensaje no sea genérico. Después hay `return false`. `ClienteDAO.guardar` no se ejecuta.
6. Si `existente` es `null`, se llama a `ClienteDAO.guardar(nuevo)`. Aquí sí se usa la clase y no la variable, porque en el proyecto ese método es `static`.
7. `guardar` devuelve `boolean`: `true` si el `INSERT` afectó filas, `false` si no insertó o si hubo `SQLException`.
8. El comentario del código de laboratorio recuerda que este `if` y el `INSERT` son dos operaciones. Entre las dos, otra conexión puede insertar el mismo documento.

En la demo, `buscarEnMemoria` imita la idea con `filter` y `findFirst()`. `cliente -> cliente.getDocumento().equals(documento)` compara en Java, carácter por carácter, distinguiendo mayúsculas. Eso sirve para practicar sin base. La solución real no debe reemplazar al DAO por este `equals`, porque la comparación de MariaDB depende de la collation de la columna.

## 7. Explicación de cada método utilizado

| Método | Estado | Acceso | Retorno |
| --- | --- | --- | --- |
| `ClienteDAO.obtenerPorDocumento(String)` | Existente | Instancia | `Cliente` o `null` |
| `ClienteDAO.guardar(Cliente)` | Existente | Estático | `boolean` |
| `registrarSiDocumentoLibre` | Nuevo | Instancia | `boolean`. `false` si falta el documento, si está repetido o si el `INSERT` falla |
| `Cliente.getDocumento()` | Existente, heredado de `Persona` | Instancia | `String` |
| `String.isBlank()` | JDK | — | `boolean` |
| `Optional.findFirst()` | Solo en la demo en memoria | — | `Optional<Cliente>` |

`MenuCliente` ya mezcla los dos estilos: el campo `clienteDAO` se usa en `obtenerTodos`, y el alta usa `ClienteDAO.guardar`. La validación nueva seguiría ese reparto.

## 8. Tipos de datos y valores de retorno

- `documento` es `String`, no un número. En SQL es `VARCHAR(30)`.
- `obtenerPorDocumento` no devuelve `boolean` ni `Optional`. El contrato real es “objeto o `null`”. Comparar con `null` es lo correcto. No llames `getNombre()` sin ese `if`.
- `guardar` devuelve `false` tanto por un fallo de conexión como por un `INSERT` que no afectó filas. El mensaje de duplicado de esta solución es anterior a esa llamada, así que no se confunde con “Error al guardar el cliente.”
- El constructor de alta deja `id` en 0 hasta que `guardar` lee la llave generada y llama a `setId`. Si rechazamos el duplicado, el objeto nuevo sigue con id 0 y no queda en la tabla.

## 9. Flujo de ejecución

Hoy, en el proyecto real:

1. `MenuCliente.registrarCliente` pide los cuatro datos.
2. Crea el `Cliente`.
3. Inserta directo.
4. Dos clientes con el mismo documento quedan guardados, porque ni Java ni la tabla lo impiden.

Flujo propuesto, todavía no conectado al menú:

1. Pedir los datos, igual que ahora.
2. Llamar a `clienteDAO.obtenerPorDocumento(documento)`.
3. Si el resultado no es `null`, mostrar el mensaje y hacer `return`. No se crea el `INSERT`.
4. Si es `null`, construir el cliente y llamar a `ClienteDAO.guardar`.

La demo en memoria hace ese guion con cuatro intentos:

1. Ana, documento `100200`: se agrega.
2. Otra persona con `100200`: mensaje con id 1 y nombre Ana Ruiz. La lista no crece.
3. Luis, documento `100300`: se agrega.
4. Documento en blanco: “El documento es obligatorio.”
5. El tamaño final es 2.

## 10. Validaciones y casos límite

- Documento repetido: mensaje y `false`. No hay `INSERT`.
- Documento nuevo: se delega en `guardar`.
- Documento vacío o solo espacios: se rechaza antes de la consulta.
- Cliente `null`: se rechaza.
- Ya existen duplicados viejos en la tabla: `obtenerPorDocumento` encuentra el primero y bloquea otro alta. No borra los duplicados anteriores.
- `documento = NULL` en SQL no se compara con `=`. Además la columna es `NOT NULL`, así que un documento nulo no debería guardarse. Por eso el método nuevo lo corta en Java.
- Mayúsculas: la demo usa `equals` y distingue `Cc100` de `cc100`. El `SELECT` real sigue la collation. Por eso, contra la base, la autoridad es `obtenerPorDocumento`, no un `equals` escrito a mano.

### Java y `UNIQUE` no son lo mismo

Validar en Java mejora el mensaje: puedes decir qué cliente ya tiene ese documento. No garantiza la unicidad. La tabla, tal como está en `Crediya.sql`, acepta dos filas con el mismo `documento`.

Una condición de carrera posible:

1. La petición A busca `100200` y obtiene `null`.
2. La petición B busca `100200` y también obtiene `null`.
3. A inserta.
4. B inserta.
5. Quedan dos clientes con el mismo documento. Los dos pasaron el `if`.

Un `UNIQUE (documento)` haría fallar el segundo `INSERT` aunque las dos búsquedas hubieran visto “no existe”. `guardar` caería en el `catch`, imprimiría el error SQL y devolvería `false`. Esa restricción no está en el script y este laboratorio no la agrega. En una respuesta oral puedes decir que el diseño completo es: validar en Java para el mensaje y declarar `UNIQUE` para la garantía. Cambiar el `CREATE TABLE` sería otro trabajo, fuera de este ejercicio.

Tampoco hay una transacción que bloquee el documento entre el `SELECT` y el `INSERT`.

## 11. Cómo se integraría conceptualmente

No está integrado. El punto exacto es `MenuCliente.registrarCliente`, después de leer `documento` y antes de `ClienteDAO.guardar`.

El menú ya tiene el campo de instancia:

```java
private static final ClienteDAO clienteDAO = new ClienteDAO();
```

Conceptualmente, el alta quedaría así. Esto no se escribió en `MenuCliente.java`:

```java
Cliente existente = clienteDAO.obtenerPorDocumento(documento);
if (existente != null) {
    System.out.printf(
            "No se puede registrar el cliente. Ya existe un cliente con el documento %s (ID %d, nombre %s).%n",
            existente.getDocumento(), existente.getId(), existente.getNombre());
    return;
}

Cliente nuevoCliente = new Cliente(nombre, documento, correo, telefono);
if (ClienteDAO.guardar(nuevoCliente)) {
    System.out.println("¡Cliente registrado con éxito!");
} else {
    System.out.println("Error al guardar el cliente.");
}
```

No hace falta una opción nueva de menú ni cambiar el `while (opcion != 4)`. No hace falta un método nuevo dentro de `ClienteDAO` para cumplir el enunciado. Si el profesor pidiera un `boolean existePorDocumento`, ese método sí sería nuevo y, por dentro, podría devolver `obtenerPorDocumento(documento) != null`.

`EmpleadoDAO` tiene el mismo hueco: `guardar` estático y `obtenerPorDocumento` de instancia, sin `UNIQUE`. No forma parte de este ejercicio, pero la pregunta es la misma.

## 12. Tres preguntas que podría hacer el profesor

**¿Por qué `obtenerPorDocumento` se llama con un objeto y `guardar` con la clase?**

Porque en `ClienteDAO` el primero es un método de instancia y el segundo es `static`. El menú real ya usa los dos estilos. Poner `static` en la búsqueda exigiría modificar el DAO, y este ejercicio no lo modifica.

**¿Qué devuelve la búsqueda si el documento no está?**

`null`. No devuelve una lista vacía. `obtenerTodos` sí devuelve lista. Confundir los dos contratos es un error típico.

**¿La validación en Java deja la tabla protegida si hay dos usuarios a la vez?**

No. Sin `UNIQUE`, los dos `INSERT` pueden tener éxito. La validación evita el duplicado en el flujo normal de un solo usuario y permite un mensaje claro. La restricción en la base es la que cierra la carrera. Hoy esa restricción no existe.

## 13. Reto

En `buscarEnMemoria`, cambia `equals` por una comparación que ignore mayúsculas y prueba `100200` contra `100200` escrito en otro caso. Después escribe, sin ejecutarlo, por qué esa prueba todavía no demuestra cómo se comportará MariaDB. La respuesta esperada menciona la collation y que el camino real sigue siendo `obtenerPorDocumento`.

## Compilar y ejecutar la demostración

```bash
javac -encoding UTF-8 --release 21 -sourcepath src -d /tmp/crediya-practica \
  PracticaExamenJava/Ejercicio11_DocumentosDuplicados/ValidadorDocumentoDuplicado.java

java -cp /tmp/crediya-practica ValidadorDocumentoDuplicado
```

La clase importa `Cliente` y `ClienteDAO`, así que no es autónoma. `main` no llama a `registrarSiDocumentoLibre`. Ese método sí haría `SELECT` e `INSERT` sobre `crediya_db`. No lo ejecutes en este repaso.
