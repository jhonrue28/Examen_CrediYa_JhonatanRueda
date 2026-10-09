package vista;

import modelo.DAO.PrestamoDAO;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import modelo.Clases.Cliente;
import modelo.Persistencia.PersistenciaArchivo;
import java.sql.SQLException;
import modelo.Clases.Prestamo;


public class Gestorprestamos {
        private static final PrestamoDAO prestamoDAO = new PrestamoDAO();
        PersistenciaArchivo persistenciaArchivo = new PersistenciaArchivo();

    public static void mostrar(){
        int opcion;
        do {
            System.out.println("\n===== MÓDULO DE PRÉSTAMOS =====");
            System.out.println("1. Registrar préstamo");
            System.out.println("2. Listar préstamos");
            System.out.println("3. Prestamos en estado");
            System.out.println("4. Volver al Menú Principal");

            opcion = ConsolUtils.leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1:
                    MenuPrestamos.crearPrestamo();
                    break;
                case 2:
                    MenuPrestamos.listarPrestamos();
                    break;
                case 3:
                    ListarPorEstado();
                    break;
                case 4:
                    System.out.println("Volviendo a el menú principal");
                    break;
                default:
                    System.out.println("Opción inválida, intente de nuevo.");
            }
        } while (opcion != 4);
    }

    private static void ListarPorEstado(){
        Scanner scanner= new Scanner (System.in);
            System.out.println("Qué estado quieres buscar? TODO EN MAYUSCULA");
            String estado = scanner.nextLine();
            obtenerporestado(estado);
            scanner.close();

    }

    public static Gestorprestamos obtenerporestado (String estado){
        List<Prestamo> lista = new ArrayList<>();
        String sql = "Select id , estado from prestamos WHERE estado= ?";
        
        
    }
    
}
