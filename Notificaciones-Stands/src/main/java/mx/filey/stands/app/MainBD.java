package mx.filey.stands.app;

import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;

import mx.filey.stands.bd.ConexionBD;
import mx.filey.stands.bd.EditorialDAO;
import mx.filey.stands.bd.ErrorBaseDatos;
import mx.filey.stands.bd.InicializadorBD;
import mx.filey.stands.bd.ReservacionDAO;
import mx.filey.stands.bd.StandDAO;
import mx.filey.stands.configuracion.Configuracion;
import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Repositorio;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;
import mx.filey.stands.negocio.CalculadoraPrecios;
import mx.filey.stands.negocio.ServicioStands;

/**
 * Demostración de la capa de base de datos: comprueba la conexión y muestra
 * el contenido de las tablas stands, editoriales y reservaciones.
 *
 * <p>Este programa no tiene reglas de negocio ni SQL: solo conecta las capas
 * (vista -> negocio -> DAO -> base de datos) y presenta los resultados.
 * La simulación con reloj, pagos y avisos sigue en {@link Main}.</p>
 */
public class MainBD {

    public static void main(String[] args) {
        try {
            ejecutar();
        } catch (ErrorBaseDatos e) {
            System.err.println("ERROR: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void ejecutar() {
        ConexionBD bd = ConexionBD.cargar();

        System.out.println("=== Conexion a la base de datos ===");
        System.out.println("Tipo : " + bd.getDescripcion());
        System.out.println("URL  : " + bd.getUrlSegura());
        System.out.println("Estado: " + probarConexion(bd));
        if (InicializadorBD.prepararSiHaceFalta(bd)) {
            System.out.println("Las tablas no existian: se crearon y se cargaron los datos de ejemplo.");
        }

        // Capa BD: un DAO por tabla. Se usan a traves de la interfaz Repositorio.
        Repositorio<Stand> stands = new StandDAO(bd);
        Repositorio<Editorial> editoriales = new EditorialDAO(bd);
        Repositorio<Reservacion> reservaciones = new ReservacionDAO(bd, stands, editoriales);

        mostrarStands(stands.listar());
        mostrarEditoriales(editoriales.listar());
        mostrarReservaciones(reservaciones.listar());

        // Capa de negocio sobre la BD: el mismo ServicioStands de siempre, sin cambios.
        ServicioStands servicioStands =
            new ServicioStands(stands, new CalculadoraPrecios(Configuracion.cargarPorDefecto()));
        System.out.println();
        System.out.println("Capas: ServicioStands (negocio) leyo " + servicioStands.listar().size()
            + " stands a traves de StandDAO (bd).");
    }

    private static String probarConexion(ConexionBD bd) {
        try (Connection conexion = bd.abrir()) {
            DatabaseMetaData datos = conexion.getMetaData();
            return "conexion exitosa (" + datos.getDatabaseProductName()
                + " " + datos.getDatabaseProductVersion() + ")";
        } catch (SQLException e) {
            throw new ErrorBaseDatos("No se pudo consultar la informacion de la base: "
                + e.getMessage(), e);
        }
    }

    // --------------------------------------------------------------- listados

    private static void mostrarStands(List<Stand> stands) {
        encabezado("STANDS", stands.size());
        System.out.printf("%-6s %8s %12s  %-10s%n", "ID", "M2", "PRECIO", "ESTADO");
        for (Stand s : stands) {
            System.out.printf("%-6s %8.1f %12s  %-10s%n", s.getId(), s.getMetrosCuadrados(),
                "$" + s.getPrecio().setScale(2, RoundingMode.HALF_UP), s.getEstado());
        }
    }

    private static void mostrarEditoriales(List<Editorial> editoriales) {
        encabezado("EDITORIALES", editoriales.size());
        System.out.printf("%-6s %-24s %s%n", "ID", "NOMBRE", "CORREO");
        for (Editorial e : editoriales) {
            System.out.printf("%-6s %-24s %s%n", e.getId(), e.getNombre(), e.getCorreo());
        }
    }

    private static void mostrarReservaciones(List<Reservacion> reservaciones) {
        encabezado("RESERVACIONES", reservaciones.size());
        System.out.printf("%-6s %-6s %-24s %-12s %-12s %s%n",
            "ID", "STAND", "EDITORIAL", "RESERVADA", "VIGENCIA", "ACTIVA");
        for (Reservacion r : reservaciones) {
            System.out.printf("%-6s %-6s %-24s %-12s %-12s %s%n", r.getId(), r.getStand().getId(),
                r.getEditorial().getNombre(), r.getFechaReserva(), r.getFechaVigencia(),
                r.isActiva() ? "si" : "no");
        }
    }

    private static void encabezado(String tabla, int total) {
        System.out.println();
        System.out.println("--- " + tabla + " (" + total + ") ---");
    }
}
