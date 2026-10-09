# Ejercicio 7. Préstamos entre dos montos

## 1. Enunciado

Pedir un monto mínimo y un monto máximo, y mostrar los préstamos cuyo monto original esté dentro de ese rango, incluidos los bordes.

Hay que rechazar montos negativos y un mínimo mayor que el máximo, usar `stream` y `filter`, mostrar con `forEach`, avisar si no hay coincidencias y explicar la lambda. Los archivos originales no se modifican.

## 2. Qué conceptos evalúa

- `Predicate` dentro de `filter`.
- `Consumer` dentro de `forEach`.
- Variables capturadas por la lambda (`minimo` y `maximo`), que deben ser efectivamente finales.
- Validar antes de consultar.
- Diferencia entre el monto original y el saldo. El filtro usa `getMonto()`.

## 3. Qué archivos originales consultaste

- `src/vista/MenuPrestamos.java`. Al crear un préstamo rechaza `monto <= 0`. Este ejercicio es distinto: el rango sí puede empezar en 0, porque el enunciado dice “positivos o cero”.
- `src/vista/ConsolUtils.java`. `leerDouble(String)` devuelve `double` y no acepta texto que no sea número. Sí acepta negativos, así que el rango hay que validarlo después.
- `src/vista/MenuReportes.java`. El patrón de reporte ya es `obtenerTodos()`, `stream()`, `filter` y `forEach`. Allí la variable de cada préstamo se llama `p`.
- `src/modelo/DAO/PrestamoDAO.java`. `obtenerTodos()` es de instancia y devuelve `List<Prestamo>`.
- `src/modelo/Clases/Prestamo.java`. `getMonto()` devuelve `double`.

## 4. Qué archivos nuevos creaste

- `PracticaExamenJava/Ejercicio07_PrestamosPorRango/FiltroPrestamosPorRango.java`
- `PracticaExamenJava/Ejercicio07_PrestamosPorRango/Explicacion.md`

## 5. Solución completa

La clase compilable es `FiltroPrestamosPorRango.java`. `main` prueba casos fijos y no pregunta por teclado ni abre la base.

```java
public void solicitarYFiltrarConBaseDeDatos() {
    double minimo = ConsolUtils.leerDouble("Monto mínimo: ");
    double maximo = ConsolUtils.leerDouble("Monto máximo: ");

    if (!rangoValido(minimo, maximo)) {
        return;
    }

    PrestamoDAO prestamoDAO = new PrestamoDAO();
    filtrarYMostrar(prestamoDAO.obtenerTodos(), minimo, maximo);
}

public boolean rangoValido(double minimo, double maximo) {
    if (minimo < 0 || maximo < 0) {
        System.out.println("Los montos deben ser positivos o cero.");
        return false;
    }
    if (minimo > maximo) {
        System.out.println("El monto mínimo no puede ser superior al monto máximo.");
        return false;
    }
    return true;
}

public void filtrarYMostrar(List<Prestamo> prestamos, double minimo, double maximo) {
    if (!rangoValido(minimo, maximo)) {
        return;
    }

    List<Prestamo> fuente = prestamos == null ? List.of() : prestamos;

    List<Prestamo> coincidencias = fuente.stream()
            .filter(prestamo -> prestamo.getMonto() >= minimo && prestamo.getMonto() <= maximo)
            .toList();

    if (coincidencias.isEmpty()) {
        System.out.println("No existen préstamos en ese rango.");
        return;
    }

    coincidencias.forEach(prestamo -> System.out.printf(
            "ID: %d | Monto: $%.2f | Estado: %s | Saldo pendiente: $%.2f%n",
            prestamo.getId(),
            prestamo.getMonto(),
            prestamo.getEstado(),
            prestamo.getSaldoPendiente()
    ));
}
```

`solicitarYFiltrarConBaseDeDatos` es el camino de examen con consola y JDBC. No se llama desde `main`.

## 6. Explicación línea por línea de las partes importantes

1. `ConsolUtils.leerDouble` escribe el mensaje, lee una línea y la convierte con `Double.parseDouble`. Si el texto no es número, vuelve a preguntar. Si el usuario escribe `-5`, lo acepta: por eso existe `rangoValido`.
2. `minimo < 0 || maximo < 0` rechaza negativos. Cero pasa, porque la comparación es estrictamente menor que cero.
3. `minimo > maximo` rechaza un intervalo al revés. Si son iguales, el rango es válido y solo entrarían préstamos con ese monto exacto.
4. El `return` de `solicitarYFiltrarConBaseDeDatos` ocurre antes de `obtenerTodos`. Un rango malo no abre la base.
5. `filtrarYMostrar` vuelve a validar para que el método sea seguro aunque lo llamen directo, como hace la demostración.
6. `fuente.stream()` no modifica la lista original.
7. `filter` recibe un `Predicate<Prestamo>`. La lambda es `prestamo -> condicion`. `prestamo` es cada elemento del stream, uno por uno. Su tipo es `Prestamo`. No hace falta declararlo: el compilador lo infiere de la lista.
8. La condición usa `>=` y `<=`, así que los bordes entran. Un préstamo de 800.000 entra en el rango 800.000 a 2.000.000.
9. `minimo` y `maximo` se capturan desde afuera. La lambda puede leerlas porque nadie les asigna un valor nuevo después. Si hicieras `minimo = 0` más abajo, el código no compilaría.
10. `toList()` cierra el stream y guarda las coincidencias. Hace falta la lista intermedia porque un `forEach` suelto no dice cuántos elementos pasaron el filtro. Sin esa lista no podrías distinguir “no hubo coincidencias” de “sí las hubo”.
11. Si `coincidencias` está vacía, se imprime el aviso y se sale. No se llama a `forEach`.
12. El segundo `prestamo ->` es otra lambda, ahora un `Consumer`. También representa cada préstamo, pero esta vez solo para imprimirlo. No devuelve un `boolean`. Cada lambda tiene su propio parámetro, aunque se llame igual.

En `MenuReportes` la misma idea se escribe `p ->`. `p` y `prestamo` son el nombre que tú eliges. No es un atributo de la clase `Prestamo`.

## 7. Explicación de cada método utilizado

| Método | Papel |
| --- | --- |
| `ConsolUtils.leerDouble(String)` | Existente y estático. Devuelve `double`. |
| `rangoValido` | Nuevo. Devuelve `boolean`. |
| `PrestamoDAO.obtenerTodos()` | Existente, de instancia. Devuelve `List<Prestamo>`. |
| `stream()` | Convierte la lista en `Stream<Prestamo>`. |
| `filter` | Operación intermedia. Deja pasar solo los que cumplen el predicado. |
| `toList()` | Operación terminal. Devuelve un `List<Prestamo>` no modificable. Desde Java 16. El proyecto usa JDK 21. La forma anterior es `collect(Collectors.toList())`. |
| `forEach` | Operación terminal sobre la lista ya filtrada. Devuelve `void`. |
| `getMonto()` | Existente. `double` con el monto original, no el saldo. |

`filter` es perezoso: no recorre nada hasta que `toList()` pide los resultados.

## 8. Tipos de datos y valores de retorno

- `minimo` y `maximo` son `double`, igual que `Prestamo.monto`.
- `rangoValido` devuelve `true` solo cuando ambos son `>= 0` y `minimo <= maximo`.
- `stream().filter(...).toList()` devuelve otra lista. Puede estar vacía. No es la misma lista que `obtenerTodos()`.
- `forEach` no devuelve la lista ni un contador.
- `prestamo` dentro de la lambda no es un campo. Existe solo mientras se evalúa ese elemento.

## 9. Flujo de ejecución

`main` no llama al método de consola. Prueba cinco llamadas sobre tres préstamos: montos 2.000.000, 5.000.000 y 800.000.

1. Mínimo `-1`: se rechaza antes del filtro.
2. Mínimo 3.000.000 y máximo 1.000.000: se rechaza por el orden.
3. Rango 800.000 a 2.000.000: entran los ids 1 y 3. El id 2 queda fuera. El orden de impresión es el orden original, no el orden por monto.
4. Rango 10 a 20: el filtro corre y la lista de coincidencias queda vacía.
5. Rango 0 a 0: es válido, pero ningún préstamo tiene monto 0, así que también avisa que no hay coincidencias.

## 10. Validaciones y casos límite

- Negativo en cualquiera de los dos extremos: mensaje y no se filtra.
- Mínimo igual al máximo: válido. Es un rango de un solo valor.
- Mínimo 0 y máximo 0: válido. El enunciado permite el cero. No copies la regla de `MenuPrestamos.crearPrestamo`, que exige monto mayor que 0 para registrar un crédito.
- Lista vacía o `null`: después de un rango válido, el mensaje es “No existen préstamos en ese rango.”
- Bordes incluidos. Si el profesor quiere excluirlos, la condición pasaría a `>` y `<`.
- El filtro no mira `getSaldoPendiente()` ni `getMontoTotal()`. Un préstamo de monto 800.000 entra aunque su saldo, ya con interés, sea 840.000.

## 11. Cómo se integraría conceptualmente

No está integrado. En el examen encajaría como una opción nueva de `MenuReportes` o de `MenuPrestamos`.

`MenuReportes` hoy termina el ciclo con `while (opcion != 4)`. Si agregas una opción y cambias el número de “Volver”, actualiza también esa condición.

Dentro del `case` llamarías a `solicitarYFiltrarConBaseDeDatos`, o escribirías esas mismas líneas usando el `prestamoDAO` que el menú ya tiene. `ConsolUtils` ya está en el paquete `vista`, así que el menú no necesita importarlo.

No se modifica `PrestamoDAO`: no hace falta un `SELECT` con `BETWEEN`. Traer la lista y filtrar en Java es lo que pide el enunciado y coincide con el estilo de `mostrarPrestamosActivos`.

## 12. Tres preguntas que podría hacer el profesor

**¿Qué representa `prestamo` en la lambda?**

Es el parámetro de la lambda, es decir, el préstamo que el stream está visitando en ese momento. En `filter` la lambda debe devolver `boolean`. En `forEach` la lambda no devuelve nada útil: solo ejecuta el `printf`.

**¿Por qué no hago `forEach` directamente sobre el `filter`?**

Porque `forEach` no informa si pasó alguien. Si nadie cumple, el programa se quedaría en silencio. Primero materializo las coincidencias, miro `isEmpty()` y solo entonces imprimo.

**¿Puedo reasignar `minimo` después de crear la lambda?**

No, si la lambda ya la capturó. Las variables locales usadas por una lambda tienen que ser efectivamente finales. Los campos de la clase no tienen esa restricción, pero aquí `minimo` es un parámetro local.

## 13. Reto

Agrega una segunda condición al `filter` para quedarte solo con estado `"PENDIENTE"`, sin borrar la condición del rango. Después prueba un préstamo `CANCELADO` cuyo monto sí cae en el rango y comprueba que no aparece. La lambda seguiría teniendo un solo parámetro `prestamo`; las dos condiciones se unen con `&&`.

## Compilar y ejecutar la demostración

```bash
javac -encoding UTF-8 --release 21 -sourcepath src -d /tmp/crediya-practica \
  PracticaExamenJava/Ejercicio07_PrestamosPorRango/FiltroPrestamosPorRango.java

java -cp /tmp/crediya-practica FiltroPrestamosPorRango
```

La clase depende de `Prestamo`, `PrestamoDAO`, `PrestamoServicio` y `ConsolUtils`. No es autónoma. `main` no usa la base. `solicitarYFiltrarConBaseDeDatos()` sí la usaría y además leería `System.in`; no lo llames durante este repaso.
