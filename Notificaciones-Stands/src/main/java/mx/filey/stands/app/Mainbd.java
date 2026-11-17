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
import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;

public class Mainbd {

    public static void main(String[] args) {
        try {
            ejecutar();
        } catch (ErrorBaseDatos e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace(); // <--- Esto mostrará la SQLException exacta y la línea del error
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

        StandDAO standDAO = new StandDAO();
        EditorialDAO editorialDAO = new EditorialDAO();
        ReservacionDAO reservacionDAO = new ReservacionDAO();

        mostrarStands(standDAO.obtenerTodos());
        mostrarEditoriales(editorialDAO.obtenerTodas());
        mostrarReservaciones(reservacionDAO.obtenerTodas());

        System.out.println();
        System.out.println("Capas: Consulta completada exitosamente a través de los DAOs y ReadQuery.");
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
