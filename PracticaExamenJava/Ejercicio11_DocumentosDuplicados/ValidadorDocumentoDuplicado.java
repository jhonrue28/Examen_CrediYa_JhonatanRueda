import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import modelo.Clases.Cliente;
import modelo.DAO.ClienteDAO;

/**
 * Ejercicio 11. Laboratorio aislado: impedir un cliente con documento repetido.
 *
 * No modifica MenuCliente ni ClienteDAO.
 *
 * Método EXISTENTE reutilizado: ClienteDAO.obtenerPorDocumento(String), de instancia.
 * Método EXISTENTE que inserta: ClienteDAO.guardar(Cliente), estático. No valida duplicados.
 * Método NUEVO: registrarSiDocumentoLibre y su equivalente en memoria.
 *
 * main() no llama a la base de datos.
 */
public class ValidadorDocumentoDuplicado {

    private final List<Cliente> clientesEnMemoria = new ArrayList<>();

    /**
     * CONEXIÓN REAL. No se invoca desde main().
     * Si el documento ya existe, no llama a ClienteDAO.guardar.
     */
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

        // La validación Java y el INSERT no son una sola operación.
        // Sin UNIQUE en clientes.documento, dos registros simultáneos pueden pasar este if.
        return ClienteDAO.guardar(nuevo);
    }

    public boolean registrarEnMemoria(Cliente nuevo) {
        if (nuevo == null || nuevo.getDocumento() == null || nuevo.getDocumento().isBlank()) {
            System.out.println("El documento es obligatorio.");
            return false;
        }

        Optional<Cliente> existente = buscarEnMemoria(nuevo.getDocumento());
        if (existente.isPresent()) {
            Cliente encontrado = existente.get();
            System.out.printf(
                    "No se puede registrar el cliente. Ya existe un cliente con el documento %s (ID %d, nombre %s).%n",
                    encontrado.getDocumento(),
                    encontrado.getId(),
                    encontrado.getNombre()
            );
            return false;
        }

        clientesEnMemoria.add(nuevo);
        System.out.println("Cliente registrado en la demostración: " + nuevo.getNombre());
        return true;
    }

    /**
     * Imita la idea de obtenerPorDocumento, pero sobre una lista.
     * La versión real debe usar el DAO, porque la comparación SQL depende de la collation.
     */
    public Optional<Cliente> buscarEnMemoria(String documento) {
        return clientesEnMemoria.stream()
                .filter(cliente -> cliente.getDocumento().equals(documento))
                .findFirst();
    }

    public static void main(String[] args) {
        System.out.println("Demostración en memoria. No se abre conexión a MariaDB.");
        System.out.println("No se llama a ClienteDAO.guardar ni a obtenerPorDocumento.");

        ValidadorDocumentoDuplicado validador = new ValidadorDocumentoDuplicado();

        Cliente ana = new Cliente(1, "Ana Ruiz", "100200", "ana@correo.com", "3001112233");
        Cliente repetida = new Cliente("Ana Copia", "100200", "otra@correo.com", "3009998877");
        Cliente luis = new Cliente("Luis Peña", "100300", "luis@correo.com", "3012223344");
        Cliente sinDocumento = new Cliente("Sin Doc", "   ", "vacio@correo.com", "3000000000");

        validador.registrarEnMemoria(ana);
        validador.registrarEnMemoria(repetida);
        validador.registrarEnMemoria(luis);
        validador.registrarEnMemoria(sinDocumento);

        System.out.println("Clientes conservados: " + validador.clientesEnMemoria.size());
    }
}
