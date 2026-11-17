package mx.filey.stands.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import mx.filey.stands.datos.Editorial;

public class EditorialDAO {

    public Editorial buscarPorId(String id) throws ErrorBaseDatos {
        String sql = "SELECT id, nombre, correo FROM editoriales WHERE id = ?";
        Editorial editorial = null;
        try (Connection conexion = ConexionBD.cargar().abrir();
             ReadQuery lector = new ReadQuery(conexion);
             ResultSet rs = lector.ejecutarConsulta(sql, id)) {
            
            if (rs.next()) {
                editorial = new Editorial(
                    rs.getString("id"),
                    rs.getString("nombre"),
                    rs.getString("correo")
                );
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("Error al buscar editorial por ID: " + id, e);
        }
        return editorial;
    }

    public List<Editorial> obtenerTodas() throws ErrorBaseDatos {
        String sql = "SELECT id, nombre, correo FROM editoriales";
        List<Editorial> editoriales = new ArrayList<>();
        try (Connection conexion = ConexionBD.cargar().abrir();
             ReadQuery lector = new ReadQuery(conexion);
             ResultSet rs = lector.ejecutarConsulta(sql)) {
            
            while (rs.next()) {
                Editorial editorial = new Editorial(
                    rs.getString("id"),
                    rs.getString("nombre"),
                    rs.getString("correo")
                );
                editoriales.add(editorial);
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("Error al obtener todas las editoriales", e);
        }
        return editoriales;
    }

    public void insertar(Editorial editorial) throws ErrorBaseDatos {
        String sql = "INSERT INTO editoriales (id, nombre, correo) VALUES (?, ?, ?)";
        try (Connection conexion = ConexionBD.cargar().abrir();
             PreparedStatement pstmt = conexion.prepareStatement(sql)) {
            
            pstmt.setString(1, editorial.getId());
            pstmt.setString(2, editorial.getNombre());
            pstmt.setString(3, editorial.getCorreo());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new ErrorBaseDatos("Error al insertar editorial", e);
        }
    }
}
