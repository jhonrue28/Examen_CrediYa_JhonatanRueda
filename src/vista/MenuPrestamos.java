package vista;

import modelo.Clases.Cliente;
import modelo.Clases.Empleado;
import modelo.Clases.Prestamo;
import modelo.DAO.ClienteDAO;
import modelo.DAO.EmpleadoDAO;
import modelo.DAO.PrestamoDAO;
import modelo.Servicios.PrestamoServicio;
import modelo.DAO.PagoDAO;
import java.util.List;

public class MenuPrestamos {

    private static final PrestamoDAO prestamoDAO = new PrestamoDAO();
    private static final ClienteDAO clienteDAO = new ClienteDAO();
    private static final EmpleadoDAO empleadoDAO = new EmpleadoDAO();
    private static final PrestamoServicio prestamoServicio = new PrestamoServicio();
    private static final PagoDAO pagoDAO = new PagoDAO();

    public static void mostrar() {
        int opcion;
        do {
            System.out.println("\n===== MÓDULO DE PRÉSTAMOS =====");
            System.out.println("1. Registrar préstamo");
            System.out.println("2. Listar préstamos");
            System.out.println("3. Buscar préstamo por ID");
            System.out.println("4. Buscar préstamos por cliente");
            System.out.println("5. Actualizar préstamo");
            System.out.println("6. Eliminar préstamo");
            System.out.println("7. Cambiar estado");
            System.out.println("8. Entrar a gestor de prestamos");
            System.out.println("9. Volver al Menú Principal");

            opcion = ConsolUtils.leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1:
                    crearPrestamo();
                    break;
                case 2:
                    listarPrestamos();
                    break;
                case 3:
                    buscarPrestamoPorId();
                    break;
                case 4:
                    consultarPrestamosCliente();
                    break;
                case 5:
                    actualizarPrestamo();
                    break;
                case 6:
                    eliminarPrestamo();
                    break;
                case 7:
                    cambiarEstado();
                    break;
                case 8:
                    Gestorprestamos.mostrar();
                case 9:
                    System.out.println("Volviendo al menú principal...");
                    break;
                default:
                    System.out.println("Opción inválida, intente de nuevo.");
            }
        } while (opcion != 8);
    }

    private static Cliente seleccionarCliente() {
        System.out.println("\n--- Buscar Cliente ---");
        System.out.println("1. Buscar por ID del Cliente");
        System.out.println("2. Buscar por Documento del Cliente");
        int opcionBusqueda = ConsolUtils.leerEntero("Seleccione una opción (1 o 2): ");

        Cliente cliente = null;
        if (opcionBusqueda == 1) {
            int idCliente = ConsolUtils.leerEntero("Ingrese el ID del Cliente: ");
            cliente = clienteDAO.obtenerPorId(idCliente);
        } else if (opcionBusqueda == 2) {
            String documento = ConsolUtils.leerTexto("Ingrese el Documento del Cliente: ");
            cliente = clienteDAO.obtenerPorDocumento(documento);
        } else {
            System.out.println("Opción de búsqueda no válida.");
            return null;
        }

        if (cliente == null) {
            System.out.println("Cliente no encontrado en el sistema.");
        }

        return cliente;
    }

    private static Empleado seleccionarEmpleado() {
        System.out.println("\n--- Buscar Empleado ---");
        System.out.println("1. Buscar por ID del Empleado");
        System.out.println("2. Buscar por Documento del Empleado");
        int opcionBusqueda = ConsolUtils.leerEntero("Seleccione una opción (1 o 2): ");

        Empleado empleado = null;
        if (opcionBusqueda == 1) {
            int idEmpleado = ConsolUtils.leerEntero("Ingrese el ID del Empleado: ");
            empleado = empleadoDAO.obtenerPorId(idEmpleado);
        } else if (opcionBusqueda == 2) {
            String documento = ConsolUtils.leerTexto("Ingrese el Documento del Empleado: ");
            empleado = empleadoDAO.obtenerPorDocumento(documento);
        } else {
            System.out.println("Opción de búsqueda no válida.");
            return null;
        }

        if (empleado == null) {
            System.out.println("Empleado no encontrado en el sistema.");
        }

        return empleado;
    }

    public  static void crearPrestamo() {
        System.out.println("\n--- Crear Nuevo Préstamo ---");

        double monto = ConsolUtils.leerDouble("Monto: ");
        double interes = ConsolUtils.leerDouble("Interés (%): ");
        int cuotas = ConsolUtils.leerEntero("Número de cuotas: ");

        if (monto <= 0) {
            System.out.println("El monto debe ser mayor que 0.");
            return;
        }

        if (interes <= 0) {
            System.out.println("El interés no puede ser negativo ni 0.");
            return;
        }

        if (cuotas <= 0) {
            System.out.println("El número de cuotas debe ser mayor que 0.");
            return;
        }

        Cliente cliente = seleccionarCliente();
        if (cliente == null) {
            return;
        }

        Empleado empleado = seleccionarEmpleado();
        if (empleado == null) {
            return;
        }

        Prestamo nuevo = new Prestamo(cliente.getId(), empleado.getId(), monto, interes, cuotas);
        prestamoServicio.calcularDatosPrestamo(nuevo);

        if (prestamoDAO.guardar(nuevo)) {
            System.out.println("==REGISTRO DE PRÉSTAMOS==");
            System.out.println("¡Préstamo registrado correctamentepara " + cliente.getNombre()
                    + " (asesor: " + empleado.getNombre() + ")!");
        } else {
            System.out.println("Error al registrar el préstamo en la base de datos.");
        }
    }

    public static void listarPrestamos() {
        System.out.println("\n--- Listado General de Préstamos ---");
        List<Prestamo> lista = prestamoDAO.obtenerTodos();

        if (lista.isEmpty()) {
            System.out.println("No hay préstamos registrados.");
        } else {
            for (Prestamo p : lista) {
                mostrarPrestamo(p);
            }
        }
    }

    private static void buscarPrestamoPorId() {
        System.out.println("\n--- Buscar Préstamo por ID ---");
        int id = ConsolUtils.leerEntero("Ingrese el ID del Préstamo: ");
        Prestamo p = prestamoDAO.obtenerPorId(id);

        if (p == null) {
            System.out.println("No existe un préstamo con el ID " + id);
            return;
        }

        mostrarPrestamo(p);
    }

    private static void consultarPrestamosCliente() {
        Cliente cliente = seleccionarCliente();
        if (cliente == null) {
            return;
        }

        List<Prestamo> lista = prestamoDAO.obtenerPorIdCliente(cliente.getId());

        if (lista.isEmpty()) {
            System.out.println("El cliente " + cliente.getNombre() + " no tiene préstamos asociados.");
        } else {
            System.out.println("\n--- Préstamos de " + cliente.getNombre() + " ---");
            for (Prestamo p : lista) {
                mostrarPrestamo(p);
            }
        }
    }

    private static void actualizarPrestamo() {
        System.out.println("\n--- Actualizar Préstamo ---");
        int id = ConsolUtils.leerEntero("Ingrese el ID del Préstamo a modificar: ");
        Prestamo prestamo = prestamoDAO.obtenerPorId(id);

        if (prestamo == null) {
            System.out.println("No existe un préstamo con el ID " + id);
            return;
        }

        System.out.println("Datos actuales:");
        mostrarPrestamo(prestamo);

        System.out.println("Deje el campo vacío para conservar el valor actual.");

        String cambiarCliente = ConsolUtils.leerTexto("¿Cambiar cliente? (S/N): ");
        if (cambiarCliente.equalsIgnoreCase("S")) {
            Cliente cliente = seleccionarCliente();
            if (cliente != null) {
                prestamo.setIdCliente(cliente.getId());
            }
        }

        String cambiarEmpleado = ConsolUtils.leerTexto("¿Cambiar empleado? (S/N): ");
        if (cambiarEmpleado.equalsIgnoreCase("S")) {
            Empleado empleado = seleccionarEmpleado();
            if (empleado != null) {
                prestamo.setIdEmpleado(empleado.getId());
            }
        }

        String montoTexto = ConsolUtils.leerTexto("Nuevo monto (actual: " + prestamo.getMonto() + "): ");
        if (!montoTexto.isEmpty()) {
            try {
                prestamo.setMonto(Double.parseDouble(montoTexto));
            } catch (NumberFormatException e) {
                System.out.println("Monto inválido. Se conserva el valor actual.");
            }
        }

        String interesTexto = ConsolUtils.leerTexto("Nuevo interés (actual: " + prestamo.getInteres() + "): ");
        if (!interesTexto.isEmpty()) {
            try {
                prestamo.setInteres(Double.parseDouble(interesTexto));
            } catch (NumberFormatException e) {
                System.out.println("Interés inválido. Se conserva el valor actual.");
            }
        }

        String cuotasTexto = ConsolUtils.leerTexto("Nuevas cuotas (actual: " + prestamo.getCuotas() + "): ");
        if (!cuotasTexto.isEmpty()) {
            try {
                prestamo.setCuotas(Integer.parseInt(cuotasTexto));
            } catch (NumberFormatException e) {
                System.out.println("Cuotas inválidas. Se conserva el valor actual.");
            }
        }

        String estadoTexto = ConsolUtils.leerTexto("Nuevo estado PENDIENTE/PAGADO/CANCELADO (actual: "
                + prestamo.getEstado() + "): ");
        if (!estadoTexto.isEmpty()) {
            String estado = estadoTexto.trim().toUpperCase();
            if (estado.equals("PENDIENTE") || estado.equals("PAGADO") || estado.equals("CANCELADO")) {
                prestamo.setEstado(estado);
            } else {
                System.out.println("Estado inválido. Se conserva el valor actual.");
            }
        }

        prestamoServicio.calcularMontoTotal(prestamo);
        prestamoServicio.calcularCuotaMensual(prestamo);
        prestamoServicio.calcularSaldoPendiente(
            prestamo,
            pagoDAO.obtenerPorPrestamo(prestamo.getId())
        );

        if (prestamoDAO.actualizar(prestamo)) {
            System.out.println("¡Préstamo actualizado exitosamente!");
        } else {
            System.out.println("Error al actualizar el préstamo.");
        }
    }

    private static void eliminarPrestamo() {
        System.out.println("\n--- Eliminar Préstamo ---");
        int id = ConsolUtils.leerEntero("Ingrese el ID del Préstamo a eliminar: ");
        Prestamo prestamo = prestamoDAO.obtenerPorId(id);

        if (prestamo == null) {
            System.out.println("No existe un préstamo con el ID " + id);
            return;
        }

        mostrarPrestamo(prestamo);
        String confirmacion = ConsolUtils.leerTexto("¿Confirmar eliminación? (S/N): ");
        if (!confirmacion.equalsIgnoreCase("S")) {
            System.out.println("Eliminación cancelada.");
            return;
        }

        if (prestamoDAO.eliminar(id)) {
            System.out.println("Préstamo eliminado correctamente.");
        } else {
            System.out.println("Error al eliminar el préstamo.");
        }
    }

    private static void cambiarEstado() {
        System.out.println("\n--- Cambiar Estado de Préstamo ---");
        int idPrestamo = ConsolUtils.leerEntero("Ingrese el ID del Préstamo a modificar: ");
        Prestamo p = prestamoDAO.obtenerPorId(idPrestamo);

        if (p == null) {
            System.out.println("No existe un préstamo con el ID " + idPrestamo);
            return;
        }

        System.out.println("Estado actual: " + p.getEstado());
        System.out.println("Seleccione el nuevo estado:");
        System.out.println("1. PENDIENTE");
        System.out.println("2. PAGADO");
        System.out.println("3. CANCELADO");
        int op = ConsolUtils.leerEntero("Opción: ");

        String nuevoEstado;
        switch (op) {
            case 1:
                nuevoEstado = "PENDIENTE";
                break;
            case 2:
                nuevoEstado = "PAGADO";
                break;
            case 3:
                nuevoEstado = "CANCELADO";
                break;
            default:
                nuevoEstado = null;
        }

        if (nuevoEstado != null) {
            if (prestamoDAO.actualizarEstado(idPrestamo, nuevoEstado)) {
                System.out.println("¡Estado actualizado exitosamente a " + nuevoEstado + "!");
            } else {
                System.out.println("Error al actualizar el estado.");
            }
        } else {
            System.out.println("Opción de estado inválida.");
        }
    }

    private static void mostrarPrestamo(Prestamo p) {
    System.out.printf(
            "ID Préstamo: %d | ID Cliente: %d | ID Empleado: %d | Monto: $%.2f | Interés: %.1f%% | Cuotas: %d | Estado: %s | Fecha inicio: %s%n",
            p.getId(),
            p.getIdCliente(),
            p.getIdEmpleado(),
            p.getMonto(),
            p.getInteres(),
            p.getCuotas(),
            p.getEstado(),
            p.getFechaInicio()
    );

    System.out.printf(
            "Monto total: $%.2f | Cuota mensual: $%.2f | Saldo pendiente: $%.2f%n",
            p.getMontoTotal(),
            p.getCuotaMensual(),
            p.getSaldoPendiente()
    );
    }
}
