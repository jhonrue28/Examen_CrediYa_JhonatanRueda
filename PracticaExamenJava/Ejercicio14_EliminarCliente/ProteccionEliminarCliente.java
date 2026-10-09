import java.util.List;
import java.util.stream.Collectors;

import modelo.Clases.Cliente;
import modelo.Clases.Prestamo;
import modelo.DAO.ClienteDAO;
import modelo.DAO.PrestamoDAO;

/**
 * Ejercicio 14. Laboratorio aislado: no eliminar un cliente que tenga préstamos.
 *
 * ClienteDAO.eliminar(int) existe y NO hace esta validación: solo ejecuta
 * DELETE FROM clientes WHERE id = ?.
 *
 * Métodos EXISTENTES reutilizados:
 * - ClienteDAO.obtenerPorId(int), de instancia
 * - PrestamoDAO.obtenerPorIdCliente(int), de instancia
 * - ClienteDAO.eliminar(int), de instancia, solo si la validación nueva lo permite
 *
 * No se usa ClienteDAO.obtenerPrestamosPorCliente(String): devuelve void,
 * imprime por consola y no permite decidir.
 *
 * Método NUEVO: decidirSiSePuedeEliminar y ejecutarConBaseDeDatos.
 * main() no llama a ejecutarConBaseDeDatos, así que no borra filas.
 */
public class ProteccionEliminarCliente {

    /**
     * CONEXIÓN REAL. No se invoca desde main().
     * Puede ejecutar DELETE solo cuando el cliente existe y no tiene préstamos.
     */
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

    /**
     * NUEVO. true solo cuando el cliente existe y la lista de préstamos está vacía.
     * No elimina nada.
     */
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

    public static void main(String[] args) {
        System.out.println("Demostración en memoria. No se abre conexión a MariaDB.");
        System.out.println("No se llama a ClienteDAO.eliminar.");

        ProteccionEliminarCliente proteccion = new ProteccionEliminarCliente();

        Cliente ana = new Cliente(1, "Ana Ruiz", "100200", "ana@correo.com", "3001112233");
        Cliente luis = new Cliente(2, "Luis Peña", "100300", "luis@correo.com", "3012223344");

        Prestamo prestamoAna = new Prestamo(ana.getId(), 1, 1_000_000, 10, 12);
        prestamoAna.setId(10);
        Prestamo otroPrestamoAna = new Prestamo(ana.getId(), 1, 400_000, 5, 6);
        otroPrestamoAna.setId(11);

        proteccion.decidirSiSePuedeEliminar(ana, List.of(prestamoAna, otroPrestamoAna));
        proteccion.decidirSiSePuedeEliminar(luis, List.of());
        proteccion.decidirSiSePuedeEliminar(null, List.of());
    }
}
