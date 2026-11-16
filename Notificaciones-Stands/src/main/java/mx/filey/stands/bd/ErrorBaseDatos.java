package mx.filey.stands.bd;

/**
 * Error al hablar con la base de datos (conexión caída, tabla inexistente, etc.).
 * Es una excepción sin chequeo para que {@code Repositorio} no tenga que declarar
 * {@code SQLException}: el resto del sistema no debe saber que existe JDBC.
 */
public class ErrorBaseDatos extends RuntimeException {

    public ErrorBaseDatos(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    public ErrorBaseDatos(String mensaje) {
        super(mensaje);
    }
}
