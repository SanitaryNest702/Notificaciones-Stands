package mx.filey.stands.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;

public class ReservacionDAO {
    private final StandDAO standDAO = new StandDAO();
    private final EditorialDAO editorialDAO = new EditorialDAO();

    public Reservacion buscarPorId(String id) throws ErrorBaseDatos {
        String sql = "SELECT id, editorial_id, stand_id, fecha_reserva, fecha_vigencia, activa FROM reservaciones WHERE id = ?";
        Reservacion reservacion = null;
        try (Connection conexion = ConexionBD.cargar().abrir();
             ReadQuery lector = new ReadQuery(conexion);
             ResultSet rs = lector.ejecutarConsulta(sql, id)) {
            
            if (rs.next()) {
                Editorial editorial = editorialDAO.buscarPorId(rs.getString("editorial_id"));
                Stand stand = standDAO.buscarPorId(rs.getString("stand_id"));
                reservacion = new Reservacion(
                    rs.getString("id"),
                    stand,
                    editorial,
                    rs.getDate("fecha_reserva").toLocalDate(),
                    rs.getDate("fecha_vigencia").toLocalDate()
                );
                if (!rs.getBoolean("activa")) {
                    reservacion.cancelar();
                }
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("Error al buscar reservación por ID: " + id, e);
        }
        return reservacion;
    }

    public List<Reservacion> obtenerTodas() throws ErrorBaseDatos {
        String sql = "SELECT id, editorial_id, stand_id, fecha_reserva, fecha_vigencia, activa FROM reservaciones";
        List<Reservacion> reservaciones = new ArrayList<>();
        try (Connection conexion = ConexionBD.cargar().abrir();
             ReadQuery lector = new ReadQuery(conexion);
             ResultSet rs = lector.ejecutarConsulta(sql)) {
            
            while (rs.next()) {
                Editorial editorial = editorialDAO.buscarPorId(rs.getString("editorial_id"));
                Stand stand = standDAO.buscarPorId(rs.getString("stand_id"));
                Reservacion r = new Reservacion(
                    rs.getString("id"),
                    stand,
                    editorial,
                    rs.getDate("fecha_reserva").toLocalDate(),
                    rs.getDate("fecha_vigencia").toLocalDate()
                );
                if (!rs.getBoolean("activa")) {
                    r.cancelar();
                }
                reservaciones.add(r);
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("Error al obtener todas las reservaciones", e);
        }
        return reservaciones;
    }

    public void insertar(Reservacion reservacion) throws ErrorBaseDatos {
        String sql = "INSERT INTO reservaciones (id, editorial_id, stand_id, fecha_reserva, fecha_vigencia, activa) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conexion = ConexionBD.cargar().abrir();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            
            pstmt.setString(1, reservacion.getId());
            pstmt.setString(2, reservacion.getEditorial().getId());
            pstmt.setString(3, reservacion.getStand().getId());
            pstmt.setObject(4, reservacion.getFechaReserva());
            pstmt.setObject(5, reservacion.getFechaVigencia());
            pstmt.setBoolean(6, reservacion.isActiva());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorBaseDatos("Error al insertar reservación", e);
        }
    }
}
