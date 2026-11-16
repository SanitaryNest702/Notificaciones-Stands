package mx.filey.stands.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Repositorio;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;

/**
 * Lee las reservaciones de la tabla {@code reservaciones}. Cada fila apunta a un stand y a una
 * editorial, que se obtienen a través de sus propios repositorios (no repite su código).
 *
 * <p>Los pagos no están en la base de datos en esta fase: las reservaciones leídas aquí
 * llegan sin pagos, por lo que no se calculan descuentos ni saldos sobre ellas.</p>
 */
public class ReservacionDAO implements Repositorio<Reservacion> {

    private static final String SELECT =
        "SELECT id, stand_id, editorial_id, fecha_reserva, fecha_vigencia, activa FROM reservaciones";

    private final ConexionBD bd;
    private final Repositorio<Stand> stands;
    private final Repositorio<Editorial> editoriales;

    public ReservacionDAO(ConexionBD bd, Repositorio<Stand> stands, Repositorio<Editorial> editoriales) {
        this.bd = bd;
        this.stands = stands;
        this.editoriales = editoriales;
    }

    @Override
    public void guardar(Reservacion reservacion) {
        throw new UnsupportedOperationException("ReservacionDAO es de solo lectura en esta fase");
    }

    @Override
    public Optional<Reservacion> buscarPorId(String id) {
        try (Connection conexion = bd.abrir();
             PreparedStatement consulta = conexion.prepareStatement(SELECT + " WHERE id = ?")) {
            consulta.setString(1, id);
            try (ResultSet fila = consulta.executeQuery()) {
                if (!fila.next()) {
                    return Optional.empty();
                }
                return Optional.of(mapear(fila,
                    idStand -> stands.buscarPorId(idStand).orElse(null),
                    idEditorial -> editoriales.buscarPorId(idEditorial).orElse(null)));
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("No se pudo leer la reservacion " + id + ": " + e.getMessage(), e);
        }
    }

    @Override
    public List<Reservacion> listar() {
        // Se leen primero los stands y editoriales (1 consulta cada una) y luego las reservaciones.
        Map<String, Stand> standsPorId = new HashMap<>();
        for (Stand stand : stands.listar()) {
            standsPorId.put(stand.getId(), stand);
        }
        Map<String, Editorial> editorialesPorId = new HashMap<>();
        for (Editorial editorial : editoriales.listar()) {
            editorialesPorId.put(editorial.getId(), editorial);
        }

        List<Reservacion> reservaciones = new ArrayList<>();
        try (Connection conexion = bd.abrir();
             PreparedStatement consulta = conexion.prepareStatement(SELECT + " ORDER BY id");
             ResultSet fila = consulta.executeQuery()) {
            while (fila.next()) {
                reservaciones.add(mapear(fila, standsPorId::get, editorialesPorId::get));
            }
        } catch (SQLException e) {
            throw new ErrorBaseDatos("No se pudo leer la tabla reservaciones: " + e.getMessage(), e);
        }
        return reservaciones;
    }

    /** Convierte una fila en una {@link Reservacion}, resolviendo su stand y su editorial. */
    private static Reservacion mapear(ResultSet fila,
                                      Function<String, Stand> buscarStand,
                                      Function<String, Editorial> buscarEditorial) throws SQLException {
        String id = fila.getString("id");
        Stand stand = buscarStand.apply(fila.getString("stand_id"));
        Editorial editorial = buscarEditorial.apply(fila.getString("editorial_id"));
        if (stand == null || editorial == null) {
            throw new ErrorBaseDatos("La reservacion " + id + " apunta a un stand o editorial inexistente");
        }
        Reservacion reservacion = new Reservacion(id, stand, editorial,
            LocalDate.parse(fila.getString("fecha_reserva")),
            LocalDate.parse(fila.getString("fecha_vigencia")));
        if (!fila.getBoolean("activa")) {
            reservacion.cancelar();
        }
        return reservacion;
    }
}
