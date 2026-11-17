package mx.filey.stands.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Stand;

public class StandDAO {

    public Stand buscarPorId(String id) throws ErrorBaseDatos {
        String sql = "SELECT id, metros_cuadrados, precio, estado FROM stands WHERE id = ?";
        Stand stand = null;
        try (Connection conexion = ConexionBD.cargar().abrir();
             ReadQuery lector = new ReadQuery(conexion);
             ResultSet rs = lector.ejecutarConsulta(sql, id)) {
            
            if (rs.next()) {
                stand = new Stand(
                    rs.getString("id"),
                    rs.getDouble("metros_cuadrados"),
                    rs.getBigDecimal("precio"),
                    EstadoStand.valueOf(rs.getString("estado"))
                );
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("Error al buscar stand por ID: " + id, e);
        }
        return stand;
    }

    public List<Stand> obtenerTodos() throws ErrorBaseDatos {
        String sql = "SELECT id, metros_cuadrados, precio, estado FROM stands";
        List<Stand> stands = new ArrayList<>();
        try (Connection conexion = ConexionBD.cargar().abrir();
             ReadQuery lector = new ReadQuery(conexion);
             ResultSet rs = lector.ejecutarConsulta(sql)) {
            
            while (rs.next()) {
                Stand stand = new Stand(
                    rs.getString("id"),
                    rs.getDouble("metros_cuadrados"),
                    rs.getBigDecimal("precio"),
                    EstadoStand.valueOf(rs.getString("estado"))
                );
                stands.add(stand);
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("Error al obtener todos los stands", e);
        }
        return stands;
    }

    public void actualizarEstado(String id, EstadoStand estado) throws ErrorBaseDatos {
        String sql = "UPDATE stands SET estado = ? WHERE id = ?";
        try (Connection conexion = ConexionBD.cargar().abrir();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            
            pstmt.setString(1, estado.name());
            pstmt.setString(2, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorBaseDatos("Error al actualizar el estado del stand", e);
        }
    }
}