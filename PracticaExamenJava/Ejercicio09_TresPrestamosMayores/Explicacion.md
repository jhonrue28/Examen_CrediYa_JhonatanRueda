# Ejercicio 9. Los tres préstamos de mayor monto

## 1. Enunciado

Mostrar como máximo los tres préstamos con mayor monto original.

Hay que usar Stream API, ordenar de mayor a menor, limitar a tres, soportar listas vacías o con uno o dos préstamos, mostrar id, monto y estado, y no modificar la lista original. El proyecto original no se toca.

## 2. Qué conceptos evalúa

- `sorted` con `Comparator`.
- `reversed()` para invertir el orden natural del comparador.
- `limit` como operación intermedia.
- `forEach` como operación terminal.
- El stream no ordena la lista de origen. `List.sort` sí lo haría.

## 3. Qué archivos originales consultaste

- `src/modelo/Clases/Prestamo.java`. `getMonto()` devuelve `double` y `getEstado()` devuelve `String`.
- `src/modelo/DAO/PrestamoDAO.java`. `obtenerTodos()` devuelve una lista nueva. No existe un método que traiga “el top 3”.
- `src/vista/MenuReportes.java`. Los reportes actuales filtran, no ordenan ni limitan.
- `src/modelo/Servicios/PrestamoServicio.java`. Sirve en la demo para dejar los objetos con la fórmula real. El orden de este ejercicio no usa el saldo.

## 4. Qué archivos nuevos creaste

- `PracticaExamenJava/Ejercicio09_TresPrestamosMayores/TresPrestamosMayores.java`
- `PracticaExamenJava/Ejercicio09_TresPrestamosMayores/Explicacion.md`

## 5. Solución completa

La clase compilable es `TresPrestamosMayores.java`.

```java
public void ejecutarConBaseDeDatos() {
    PrestamoDAO prestamoDAO = new PrestamoDAO();
    mostrarTresMayores(prestamoDAO.obtenerTodos());
}

public void mostrarTresMayores(List<Prestamo> prestamos) {
    System.out.println("\n--- Hasta tres préstamos de mayor monto original ---");

    List<Prestamo> fuente = prestamos == null ? List.of() : prestamos;

    List<Prestamo> mayores = fuente.stream()
            .sorted(Comparator.comparingDouble(Prestamo::getMonto).reversed())
            .limit(LIMITE)
            .toList();

    if (mayores.isEmpty()) {
        System.out.println("No hay préstamos registrados.");
        return;
    }

    System.out.println("Resultados mostrados: " + mayores.size());
    mayores.forEach(prestamo -> System.out.printf(
            "ID: %d | Monto: $%.2f | Estado: %s%n",
            prestamo.getId(),
            prestamo.getMonto(),
            prestamo.getEstado()
    ));
}
```

`LIMITE` vale 3. `ejecutarConBaseDeDatos` no se llama desde `main`.

## 6. Explicación línea por línea de las partes importantes

1. `obtenerTodos()` trae todos los préstamos. El límite a tres se hace en Java, no en el `SELECT`. El DAO real no tiene `LIMIT`.
2. Si la lista llega `null`, se sustituye por una lista vacía. `stream()` sobre `null` lanzaría `NullPointerException`.
3. `stream()` no altera `fuente`.
4. `Comparator.comparingDouble(Prestamo::getMonto)` ordena de menor a mayor, porque ese es el orden natural de los `double`.
5. `.reversed()` da la vuelta a ese comparador: mayor monto primero.
6. La forma expandida del mismo orden es `(a, b) -> Double.compare(b.getMonto(), a.getMonto())`. Comparar `b` contra `a`, y no `a` contra `b`, invierte el orden.
7. `limit(3)` deja seguir como máximo tres elementos. Si el stream trae menos, salen todos los que hay. No rellena con nulos y no lanza una excepción por “faltan elementos”.
8. `toList()` termina el stream y produce otra lista. Esa lista nueva es la que tiene el orden de mayor a menor.
9. Si quedó vacía, se avisa y no se entra al `forEach`.
10. `mayores.size()` puede ser 1, 2 o 3. El mensaje lo dice para que se vea el caso de “menos de tres”.
11. `forEach` recibe `prestamo`, cada uno de los que sobrevivieron al `limit`, ya ordenados.
12. La demo imprime los ids antes y después. Siguen `3, 1, 2, 4, 5`. El reporte no llamó a `sort` sobre la lista original.

Detalle que suelen preguntar: `limit` no evita ordenar toda la lista. `sorted` necesita ver todos los elementos antes de saber cuáles son los tres primeros. El `limit` solo descarta el resto después de ese orden.

## 7. Explicación de cada método utilizado

| Método | Qué hace | Qué devuelve |
| --- | --- | --- |
| `stream()` | Abre el flujo | `Stream<Prestamo>` |
| `sorted(Comparator)` | Ordena dentro del flujo | otro `Stream<Prestamo>` |
| `Comparator.comparingDouble` | Compara por un `double` | `Comparator<Prestamo>` |
| `reversed()` | Invierte ese comparador | otro `Comparator<Prestamo>` |
| `limit(long)` | Corta el flujo | otro `Stream<Prestamo>` |
| `toList()` | Materializa el resultado | `List<Prestamo>` |
| `forEach(Consumer)` | Ejecuta la impresión | `void` |
| `PrestamoDAO.obtenerTodos()` | Existente, de instancia | `List<Prestamo>` |

`sorted`, `limit` y `filter` son intermedias. No recorren solas. `toList` y `forEach` son terminales. Un stream solo admite una operación terminal.

## 8. Tipos de datos y valores de retorno

- `LIMITE` es `int`, pero `limit` recibe `long`. El `int` se convierte solo.
- `comparingDouble` trabaja con el primitivo `double` de `getMonto()`. `Comparator.comparing(Prestamo::getMonto)` también compilaría, pero empaquetaría cada monto en un `Double`. Para este proyecto, `comparingDouble` es la forma directa.
- `getId()` es `int`. `getEstado()` es `String`.
- La lista de `toList()` no admite `add`. Eso no importa: el reporte solo la lee.
- `mayores` y `prestamos` son listas distintas. Comparten los mismos objetos `Prestamo`, no el orden de la lista.

## 9. Flujo de ejecución

La demo crea cinco préstamos, en este orden de ids: 3 (800.000), 1 (2.000.000), 2 (5.000.000), 4 (1.500.000), 5 (3.000.000).

1. Imprime el orden original.
2. El reporte muestra 3 resultados: id 2, id 5, id 1.
3. Vuelve a imprimir el orden original, sin cambios.
4. Pasa solo los dos primeros (`subList` es parte de la demo, no del reporte). Muestra 2: primero el id 1, luego el id 3.
5. Pasa un solo préstamo. Muestra 1, el id 3.
6. Pasa una lista vacía. Mensaje y ningún `printf` de préstamo.

`subList` en la demo es una vista de la lista original. El reporte no la ordena. No uses `subList` como solución del ejercicio.

## 10. Validaciones y casos límite

- Cero préstamos: mensaje, sin excepción.
- Uno o dos préstamos: se muestran todos, de mayor a menor. `limit(3)` no exige que existan tres.
- Exactamente tres: se muestran los tres.
- Más de tres: se muestran solo los tres montos más altos.
- Empate de monto: `sorted` de un stream secuencial es estable, así que conserva el orden relativo en el que venían.
- Lista `null`: se trata como vacía.
- No se llama a `prestamos.sort(...)`. Ese método sí modificaría la lista que devolvió el DAO.

## 11. Cómo se integraría conceptualmente

No está integrado. El lugar natural es una opción nueva de `MenuReportes`, usando el `prestamoDAO` que ya está declarado.

Ojo con el ciclo: hoy es `while (opcion != 4)` y 4 significa volver. Si “Volver” deja de ser 4, la condición del `while` también cambia.

No hace falta un método nuevo en `PrestamoDAO` ni una columna nueva. Ordenar en Java cumple el enunciado y deja el `SELECT` actual igual.

En el examen, si te piden no gastar memoria de más, di con honestidad que `sorted` igual ordena todo el stream. Limitar a tres no convierte el algoritmo en un “top 3” parcial. Para la cantidad de préstamos de CrediYa, esa claridad importa más que microoptimizar.

## 12. Tres preguntas que podría hacer el profesor

**¿Qué hace cada operación del pipeline?**

`sorted` ordena de mayor a menor monto. `limit(3)` se queda con los tres primeros de ese orden, o con menos si no hay tres. `forEach` imprime cada sobreviviente. `toList` está en medio para poder detectar la lista vacía antes de imprimir.

**¿La lista original queda ordenada?**

No. El orden vive en el stream y en la lista nueva. La demo lo comprueba: los ids siguen siendo `3, 1, 2, 4, 5`.

**¿Qué pasa con `prestamos.get(2)` si solo hay un préstamo?**

Lanza `IndexOutOfBoundsException`. Por eso la solución no pide la posición 0, 1 y 2 a mano. `limit` expresa “como máximo tres” sin asumir que existen.

## 13. Reto

Convierte `LIMITE` en un parámetro del método: `mostrarMayores(List<Prestamo> prestamos, int limite)`. Si `limite` es 0 o negativo, muestra un mensaje y no armes el stream. Prueba con 1 y con 5 sobre los mismos cinco préstamos. El de 5 debe mostrar los cinco, porque no hay más.

## Compilar y ejecutar la demostración

```bash
javac -encoding UTF-8 --release 21 -sourcepath src -d /tmp/crediya-practica \
  PracticaExamenJava/Ejercicio09_TresPrestamosMayores/TresPrestamosMayores.java

java -cp /tmp/crediya-practica TresPrestamosMayores
```

Depende de las clases de `src/`. `main` no conecta a MariaDB. `ejecutarConBaseDeDatos()` sí leería la base real.
