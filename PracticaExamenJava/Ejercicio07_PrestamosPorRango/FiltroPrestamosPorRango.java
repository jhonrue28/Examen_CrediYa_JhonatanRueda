import java.util.ArrayList;
import java.util.List;

import modelo.Clases.Prestamo;
import modelo.DAO.PrestamoDAO;
import modelo.Servicios.PrestamoServicio;
import vista.ConsolUtils;

/**
 * Ejercicio 7. Laboratorio aislado: préstamos cuyo monto original cae en un rango.
 *
 * No está integrado en MenuReportes ni en MenuPrestamos.
 *
 * main() solo ejecuta casos fijos en memoria.
 * solicitarYFiltrarConBaseDeDatos() pediría datos por consola y abriría MariaDB.
 * Ese método no se llama desde main().
 */
public class FiltroPrestamosPorRango {

    /**
     * CONEXIÓN REAL Y CONSOLA. No se invoca desde main().
     * Valida antes de consultar. PrestamoDAO.obtenerTodos() es de instancia.
     */
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

        System.out.printf("%n--- Préstamos con monto original entre $%.2f y $%.2f ---%n", minimo, maximo);

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

    public static void main(String[] args) {
        System.out.println("Demostración en memoria. No se abre conexión a MariaDB.");

        FiltroPrestamosPorRango filtro = new FiltroPrestamosPorRango();
        List<Prestamo> prestamos = crearDatosDePractica();

        filtro.filtrarYMostrar(prestamos, -1, 1_000_000);
        filtro.filtrarYMostrar(prestamos, 3_000_000, 1_000_000);
        filtro.filtrarYMostrar(prestamos, 800_000, 2_000_000);
        filtro.filtrarYMostrar(prestamos, 10, 20);
        filtro.filtrarYMostrar(prestamos, 0, 0);
    }

    private static List<Prestamo> crearDatosDePractica() {
        PrestamoServicio servicio = new PrestamoServicio();
        List<Prestamo> prestamos = new ArrayList<>();

        prestamos.add(preparar(servicio, 1, 2_000_000, 10, 10));
        prestamos.add(preparar(servicio, 2, 5_000_000, 10, 10));
        prestamos.add(preparar(servicio, 3, 800_000, 5, 4));
        return prestamos;
    }

    private static Prestamo preparar(PrestamoServicio servicio, int id, double monto, double interes, int cuotas) {
        Prestamo prestamo = new Prestamo(1, 1, monto, interes, cuotas);
        prestamo.setId(id);
        servicio.calcularDatosPrestamo(prestamo);
        return prestamo;
    }
}
