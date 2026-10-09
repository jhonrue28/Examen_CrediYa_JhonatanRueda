package modelo.DAO;

import modelo.Clases.Prestamo;
import modelo.Persistencia.ConexionBD;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import modelo.Servicios.PrestamoServicio;
import modelo.Clases.Pago;
import modelo.Persistencia.PersistenciaArchivo;

public class PrestamoDAO {

    public boolean guardar(Prestamo prestamo) {
        PersistenciaArchivo persistenciaArchivo = new PersistenciaArchivo();
        String sql = "INSERT INTO prestamos (cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Date fechaInicio = prestamo.getFechaInicio();
            if (fechaInicio == null) {
                fechaInicio = new Date(System.currentTimeMillis());
                prestamo.setFechaInicio(fechaInicio);
            }

            ps.setInt(1, prestamo.getIdCliente());
            ps.setInt(2, prestamo.getIdEmpleado());
            ps.setDouble(3, prestamo.getMonto());
            ps.setDouble(4, prestamo.getInteres());
            ps.setInt(5, prestamo.getCuotas());
            ps.setDate(6, fechaInicio);
            ps.setString(7, prestamo.getEstado());

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        prestamo.setId(keys.getInt(1));
                    }
                }
                String linea = prestamo.getId()
                    + "|" + prestamo.getIdCliente()
                    + "|" + prestamo.getIdEmpleado()
                    + "|" + prestamo.getMonto()
                    + "|" + prestamo.getInteres()
                    + "|" + prestamo.getCuotas()
                    + "|" + prestamo.getFechaInicio()
                    + "|" + prestamo.getEstado();

                persistenciaArchivo.guardarPrestamo(linea);

                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error al registrar préstamo: " + e.getMessage());
            return false;
        }
    }

    public List<Prestamo> obtenerTodos() {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT id, cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado FROM prestamos";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearPrestamo(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar préstamos: " + e.getMessage());
        }
        return lista;
    }

    public Prestamo obtenerPorId(int id) {
        String sql = "SELECT id, cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado "
                + "FROM prestamos WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPrestamo(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar préstamo por ID: " + e.getMessage());
        }
        return null;
    }
    public Prestamo obtenerPorEstado(String estado) {
        String sql = "SELECT id, estado "
                + "FROM prestamos WHERE estado = ?";

        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapearPrestamo(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar préstamo por estado: " + e.getMessage());
        }
        return null;
    }

    public List<Prestamo> obtenerPorIdCliente(int idCliente) {
        List<Prestamo> lista = new ArrayList<>();
        String sql = "SELECT id, cliente_id, empleado_id, monto, interes, cuotas, fecha_inicio, estado "
                + "FROM prestamos WHERE cliente_id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearPrestamo(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar préstamos por ID del cliente: " + e.getMessage());
        }
        return lista;
    }

    public boolean actualizar(Prestamo prestamo) {
        String sql = "UPDATE prestamos SET cliente_id = ?, empleado_id = ?, monto = ?, interes = ?, "
                + "cuotas = ?, fecha_inicio = ?, estado = ? WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            Date fechaInicio = prestamo.getFechaInicio();
            if (fechaInicio == null) {
                fechaInicio = new Date(System.currentTimeMillis());
            }

            ps.setInt(1, prestamo.getIdCliente());
            ps.setInt(2, prestamo.getIdEmpleado());
            ps.setDouble(3, prestamo.getMonto());
            ps.setDouble(4, prestamo.getInteres());
            ps.setInt(5, prestamo.getCuotas());
            ps.setDate(6, fechaInicio);
            ps.setString(7, prestamo.getEstado());
            ps.setInt(8, prestamo.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar préstamo: " + e.getMessage());
            return false;
        }
    }

    public boolean eliminar(int id) {
    PagoDAO pagoDAO = new PagoDAO();

    if (!pagoDAO.obtenerPorPrestamo(id).isEmpty()) {
        System.out.println("No se puede eliminar el préstamo porque tiene pagos registrados.");
        return false;
    }

    String sql = "DELETE FROM prestamos WHERE id = ?";

    try (Connection con = ConexionBD.obtenerConexion();
        PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id);
        return ps.executeUpdate() > 0;

    } catch (SQLException e) {
        System.err.println("Error al eliminar préstamo: " + e.getMessage());
        return false;
    }
}

    public boolean actualizarEstado(int id, String nuevoEstado) {
        String sql = "UPDATE prestamos SET estado = ? WHERE id = ?";

        try (Connection con = ConexionBD.obtenerConexion();
            PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del préstamo: " + e.getMessage());
            return false;
        }
    }

    private Prestamo mapearPrestamo(ResultSet rs) throws SQLException {
    Prestamo prestamo = new Prestamo(
            rs.getInt("id"),
            rs.getInt("cliente_id"),
            rs.getInt("empleado_id"),
            rs.getDouble("monto"),
            rs.getDouble("interes"),
            rs.getInt("cuotas"),
            rs.getString("estado"),
            rs.getDate("fecha_inicio"),
            0,
            0,
            0
    );

    PrestamoServicio servicio = new PrestamoServicio();
    PagoDAO pagoDAO = new PagoDAO();
    List<Pago> pagos = pagoDAO.obtenerPorPrestamo(prestamo.getId());

    servicio.calcularMontoTotal(prestamo);
    servicio.calcularCuotaMensual(prestamo);
    servicio.calcularSaldoPendiente(prestamo, pagos);

    return prestamo;
    }
}
