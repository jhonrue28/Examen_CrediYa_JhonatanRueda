import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import modelo.Clases.Pago;
import modelo.Clases.Prestamo;
import modelo.DAO.PagoDAO;
import modelo.DAO.PrestamoDAO;
import modelo.Servicios.PrestamoServicio;

/**
 * Ejercicio 13. Laboratorio aislado: total pagado y saldo pendiente por préstamo.
 *
 * No modifica PrestamoDAO, PagoDAO ni PrestamoServicio.
 *
 * Fórmula existente en PrestamoServicio.calcularMontoTotal:
 *     montoTotal = monto + (monto * interes / 100)
 * Fórmula existente en PrestamoServicio.calcularSaldoPendiente:
 *     saldo = montoTotal - suma de Pago.getMonto()
 *     si saldo < 0, saldo = 0
 *     si saldo == 0, el objeto queda con estado PAGADO (solo en memoria)
 *
 * main() no consulta MariaDB.
 */
public class ReportePagosYSaldos {

    /**
     * CONEXIÓN REAL. No se invoca desde main().
     *
     * obtenerTodos() de préstamos ya dispara, dentro de mapearPrestamo,
     * una consulta de pagos por cada préstamo. Por eso el total pagado de este
     * reporte sale de un único pagoDAO.obtenerTodos(), no de otro obtenerPorPrestamo
     * dentro del ciclo.
     */
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

    public static void main(String[] args) {
        System.out.println("Demostración en memoria. No se abre conexión a MariaDB.");

        ReportePagosYSaldos reporte = new ReportePagosYSaldos();
        PrestamoServicio servicio = new PrestamoServicio();
        List<Prestamo> prestamos = new ArrayList<>();
        List<Pago> pagos = new ArrayList<>();

        Prestamo sinPagos = crearPrestamo(1, 1_000_000, 10, 12);
        servicio.calcularDatosPrestamo(sinPagos);
        prestamos.add(sinPagos);

        Prestamo conPagos = crearPrestamo(2, 2_000_000, 10, 10);
        servicio.calcularDatosPrestamo(conPagos);
        List<Pago> pagosDelSegundo = List.of(new Pago(2, 500_000), new Pago(2, 200_000));
        servicio.calcularSaldoPendiente(conPagos, pagosDelSegundo);
        prestamos.add(conPagos);
        pagos.addAll(pagosDelSegundo);

        Prestamo sinInteres = crearPrestamo(3, 500_000, 0, 5);
        servicio.calcularDatosPrestamo(sinInteres);
        prestamos.add(sinInteres);

        Prestamo pagadoDeMas = crearPrestamo(4, 100_000, 0, 1);
        servicio.calcularDatosPrestamo(pagadoDeMas);
        List<Pago> pagoExcedido = List.of(new Pago(4, 150_000));
        servicio.calcularSaldoPendiente(pagadoDeMas, pagoExcedido);
        prestamos.add(pagadoDeMas);
        pagos.addAll(pagoExcedido);

        reporte.generar(prestamos, pagos);
        reporte.generar(List.of(), List.of());
    }

    private static Prestamo crearPrestamo(int id, double monto, double interes, int cuotas) {
        Prestamo prestamo = new Prestamo(1, 1, monto, interes, cuotas);
        prestamo.setId(id);
        return prestamo;
    }
}
