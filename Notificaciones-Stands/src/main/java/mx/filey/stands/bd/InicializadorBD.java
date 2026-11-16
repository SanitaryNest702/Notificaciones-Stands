package mx.filey.stands.bd;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Crea las tablas y carga los datos de ejemplo si todavía no existen.
 * Los scripts están en {@code src/main/resources/bd/} ({@code schema.sql} y {@code datos.sql}).
 * Es un paso de preparación; la lectura de las entidades la hacen los DAOs.
 */
public final class InicializadorBD {

    private static final String[] TABLAS = {"stands", "editoriales", "reservaciones"};
    private static final String[] SCRIPTS = {"/bd/schema.sql", "/bd/datos.sql"};

    private InicializadorBD() {
    }

    /**
     * Si falta alguna de las 3 tablas, ejecuta los scripts de esquema y datos.
     *
     * @return {@code true} si hubo que crear las tablas; {@code false} si ya existían
     */
    public static boolean prepararSiHaceFalta(ConexionBD bd) {
        try (Connection conexion = bd.abrir()) {
            if (existenLasTablas(conexion)) {
                return false;
            }
            ejecutarScripts(conexion);
            return true;
        } catch (SQLException e) {
            throw new ErrorBaseDatos("No se pudieron preparar las tablas: " + e.getMessage(), e);
        }
    }

    private static boolean existenLasTablas(Connection conexion) {
        for (String tabla : TABLAS) {
            try (Statement consulta = conexion.createStatement();
                 ResultSet resultado = consulta.executeQuery("SELECT COUNT(*) FROM " + tabla)) {
                resultado.next();
            } catch (SQLException e) {
                return false; // la tabla no existe todavia
            }
        }
        return true;
    }

    private static void ejecutarScripts(Connection conexion) throws SQLException {
        boolean autoCommitOriginal = conexion.getAutoCommit();
        conexion.setAutoCommit(false);
        try (Statement sentencia = conexion.createStatement()) {
            for (String script : SCRIPTS) {
                for (String sql : separarSentencias(leerRecurso(script))) {
                    sentencia.execute(sql);
                }
            }
            conexion.commit();
        } catch (SQLException e) {
            conexion.rollback();
            throw e;
        } finally {
            conexion.setAutoCommit(autoCommitOriginal);
        }
    }

    private static String leerRecurso(String ruta) {
        try (InputStream entrada = InicializadorBD.class.getResourceAsStream(ruta)) {
            if (entrada == null) {
                throw new ErrorBaseDatos("No se encontro el script " + ruta);
            }
            return new String(entrada.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ErrorBaseDatos("No se pudo leer el script " + ruta, e);
        }
    }

    /** Quita los comentarios ("-- ...") y separa el script en sentencias por el punto y coma. */
    static List<String> separarSentencias(String script) {
        StringBuilder sinComentarios = new StringBuilder();
        for (String linea : script.split("\\R")) {
            if (!linea.trim().startsWith("--")) {
                sinComentarios.append(linea).append('\n');
            }
        }
        List<String> sentencias = new ArrayList<>();
        for (String parte : sinComentarios.toString().split(";")) {
            if (!parte.isBlank()) {
                sentencias.add(parte.trim());
            }
        }
        return sentencias;
    }
}
