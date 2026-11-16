package mx.filey.stands.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Repositorio;
import mx.filey.stands.datos.Stand;

/**
 * Lee los stands de la tabla {@code stands}. Implementa {@link Repositorio}, así que
 * los servicios de negocio pueden usarlo igual que al repositorio en memoria.
 * En esta fase es de solo lectura.
 */
public class StandDAO implements Repositorio<Stand> {

    private static final String SELECT =
        "SELECT id, metros_cuadrados, precio, estado FROM stands";

    private final ConexionBD bd;

    public StandDAO(ConexionBD bd) {
        this.bd = bd;
    }

    @Override
    public void guardar(Stand stand) {
        throw new UnsupportedOperationException("StandDAO es de solo lectura en esta fase");
    }

    @Override
    public Optional<Stand> buscarPorId(String id) {
        try (Connection conexion = bd.abrir();
             PreparedStatement consulta = conexion.prepareStatement(SELECT + " WHERE id = ?")) {
            consulta.setString(1, id);
            try (ResultSet fila = consulta.executeQuery()) {
                return fila.next() ? Optional.of(mapear(fila)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("No se pudo leer el stand " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Stand> listar() {
        List<Stand> stands = new ArrayList<>();
        try (Connection conexion = bd.abrir();
             PreparedStatement consulta = conexion.prepareStatement(SELECT + " ORDER BY id");
             ResultSet fila = consulta.executeQuery()) {
            while (fila.next()) {
                stands.add(mapear(fila));
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("No se pudo leer la tabla stands: " + e.getMessage(), e);
        }
        return stands;
    }

    /** Convierte una fila de la tabla en un objeto {@link Stand}. */
    private static Stand mapear(ResultSet fila) throws SQLException {
        Stand stand = new Stand(
            fila.getString("id"),
            fila.getDouble("metros_cuadrados"),
            fila.getBigDecimal("precio"));
        restaurarEstado(stand, EstadoStand.valueOf(fila.getString("estado")));
        return stand;
    }

    /**
     * Un stand nuevo siempre nace DISPONIBLE y solo admite transiciones validas.
     * Para dejarlo en el estado guardado se recorre el camino permitido
     * (por ejemplo OCUPADO se alcanza pasando por RESERVADO) sin saltarse las reglas.
     */
    private static void restaurarEstado(Stand stand, EstadoStand estado) {
        switch (estado) {
            case DISPONIBLE:
                break;
            case RESERVADO:
                stand.cambiarEstado(EstadoStand.RESERVADO);
                break;
            case OCUPADO:
                stand.cambiarEstado(EstadoStand.RESERVADO);
                stand.cambiarEstado(EstadoStand.OCUPADO);
                break;
            case BLOQUEADO:
                stand.cambiarEstado(EstadoStand.BLOQUEADO);
                break;
            default:
                throw new IllegalStateException("Estado desconocido: " + estado);
        }
    }
}
