import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import modelo.Clases.Pago;
import modelo.Clases.Prestamo;
import modelo.DAO.PrestamoDAO;
import modelo.Servicios.PrestamoServicio;

/**
 * Ejercicio 4. Laboratorio aislado: préstamo de mayor monto original.
 *
 * No está integrado en MenuReportes. No modifica ningún archivo de src/.
 *
 * main() solo ejecuta la demostración en memoria.
 * ejecutarConBaseDeDatos() sí usaría MariaDB y no se llama desde main().
 */
public class ReportePrestamoMayorMonto {

    /**
     * CONEXIÓN REAL. No se invoca desde main().
     * Reutiliza PrestamoDAO.obtenerTodos(), método de instancia ya existente.
     * Dentro de ese método, mapearPrestamo ya calcula montoTotal y saldoPendiente.
     */
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

    /**
     * Contraste pedagógico. No es el reporte pedido.
     * Compara getSaldoPendiente() en lugar de getMonto().
     */
    public void mostrarPrestamoDeMayorSaldo(List<Prestamo> prestamos) {
        System.out.println("\n--- Contraste: préstamo de mayor saldo pendiente ---");

        List<Prestamo> fuente = prestamos == null ? List.of() : prestamos;

        Optional<Prestamo> mayor = fuente.stream()
                .max(Comparator.comparingDouble(Prestamo::getSaldoPendiente));

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

    public static void main(String[] args) {
        System.out.println("Demostración en memoria. No se abre conexión a MariaDB.");

        ReportePrestamoMayorMonto reporte = new ReportePrestamoMayorMonto();
        List<Prestamo> prestamos = crearDatosDePractica();

        reporte.mostrarPrestamoDeMayorMonto(prestamos);
        reporte.mostrarPrestamoDeMayorSaldo(prestamos);
        reporte.mostrarPrestamoDeMayorMonto(new ArrayList<>());
    }

    /**
     * Arma préstamos con PrestamoServicio, la misma fórmula del proyecto.
     * El préstamo de mayor monto original no es el de mayor saldo.
     */
    private static List<Prestamo> crearDatosDePractica() {
        PrestamoServicio servicio = new PrestamoServicio();

        Prestamo casiPagado = crearPrestamo(2, 5_000_000, 10, 10);
        servicio.calcularDatosPrestamo(casiPagado);
        servicio.calcularSaldoPendiente(casiPagado, List.of(new Pago(2, 5_000_000)));

        Prestamo saldoAlto = crearPrestamo(1, 2_000_000, 10, 10);
        servicio.calcularDatosPrestamo(saldoAlto);

        Prestamo pequeno = crearPrestamo(3, 800_000, 5, 4);
        servicio.calcularDatosPrestamo(pequeno);

        List<Prestamo> prestamos = new ArrayList<>();
        prestamos.add(saldoAlto);
        prestamos.add(casiPagado);
        prestamos.add(pequeno);
        return prestamos;
    }

    private static Prestamo crearPrestamo(int id, double monto, double interes, int cuotas) {
        Prestamo prestamo = new Prestamo(1, 1, monto, interes, cuotas);
        prestamo.setId(id);
        return prestamo;
    }
}
