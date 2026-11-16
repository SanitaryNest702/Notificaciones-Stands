package mx.filey.stands.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Repositorio;

/**
 * Lee las editoriales de la tabla {@code editoriales}. En esta fase es de solo lectura.
 */
public class EditorialDAO implements Repositorio<Editorial> {

    private static final String SELECT = "SELECT id, nombre, correo FROM editoriales";

    private final ConexionBD bd;

    public EditorialDAO(ConexionBD bd) {
        this.bd = bd;
    }

    @Override
    public void guardar(Editorial editorial) {
        throw new UnsupportedOperationException("EditorialDAO es de solo lectura en esta fase");
    }

    @Override
    public Optional<Editorial> buscarPorId(String id) {
        try (Connection conexion = bd.abrir();
             PreparedStatement consulta = conexion.prepareStatement(SELECT + " WHERE id = ?")) {
            consulta.setString(1, id);
            try (ResultSet fila = consulta.executeQuery()) {
                return fila.next() ? Optional.of(mapear(fila)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("No se pudo leer la editorial " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Editorial> listar() {
        List<Editorial> editoriales = new ArrayList<>();
        try (Connection conexion = bd.abrir();
             PreparedStatement consulta = conexion.prepareStatement(SELECT + " ORDER BY id");
             ResultSet fila = consulta.executeQuery()) {
            while (fila.next()) {
                editoriales.add(mapear(fila));
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("No se pudo leer la tabla editoriales: " + e.getMessage(), e);
        }
        return editoriales;
    }

    private static Editorial mapear(ResultSet fila) throws SQLException {
        return new Editorial(
            fila.getString("id"),
            fila.getString("nombre"),
            fila.getString("correo"));
    }
}
