import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import modelo.Clases.Prestamo;
import modelo.DAO.PrestamoDAO;
import modelo.Servicios.PrestamoServicio;

/**
 * Ejercicio 9. Laboratorio aislado: como máximo los tres préstamos de mayor monto original.
 *
 * No está integrado en el menú. El stream no reordena la lista original.
 *
 * main() solo usa datos en memoria.
 * ejecutarConBaseDeDatos() no se llama desde main().
 */
public class TresPrestamosMayores {

    private static final int LIMITE = 3;

    /**
     * CONEXIÓN REAL. No se invoca desde main().
     * Reutiliza PrestamoDAO.obtenerTodos(), método de instancia existente.
     */
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

    public static void main(String[] args) {
        System.out.println("Demostración en memoria. No se abre conexión a MariaDB.");

        TresPrestamosMayores reporte = new TresPrestamosMayores();
        List<Prestamo> prestamos = crearDatosDePractica();

        System.out.println("Orden original de IDs: " + ids(prestamos));
        reporte.mostrarTresMayores(prestamos);
        System.out.println("Orden original después del reporte: " + ids(prestamos));

        reporte.mostrarTresMayores(prestamos.subList(0, 2));
        reporte.mostrarTresMayores(List.of(prestamos.get(0)));
        reporte.mostrarTresMayores(new ArrayList<>());
    }

    private static List<Prestamo> crearDatosDePractica() {
        PrestamoServicio servicio = new PrestamoServicio();
        List<Prestamo> prestamos = new ArrayList<>();
        prestamos.add(preparar(servicio, 3, 800_000, 5, 4));
        prestamos.add(preparar(servicio, 1, 2_000_000, 10, 10));
        prestamos.add(preparar(servicio, 2, 5_000_000, 10, 10));
        prestamos.add(preparar(servicio, 4, 1_500_000, 8, 6));
        prestamos.add(preparar(servicio, 5, 3_000_000, 12, 12));
        return prestamos;
    }

    private static Prestamo preparar(PrestamoServicio servicio, int id, double monto, double interes, int cuotas) {
        Prestamo prestamo = new Prestamo(1, 1, monto, interes, cuotas);
        prestamo.setId(id);
        servicio.calcularDatosPrestamo(prestamo);
        return prestamo;
    }

    private static String ids(List<Prestamo> prestamos) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < prestamos.size(); i++) {
            if (i > 0) {
                texto.append(", ");
            }
            texto.append(prestamos.get(i).getId());
        }
        return texto.toString();
    }
}
