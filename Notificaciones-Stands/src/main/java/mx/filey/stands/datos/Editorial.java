package mx.filey.stands.datos;

/**
 * Empresa que compra o reserva stands.
 */
public class Editorial {
    private final String id;
    private final String nombre;
    private final String correo;

    public Editorial(String id, String nombre, String correo) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    @Override
    public String toString() {
        return nombre + " <" + correo + ">";
    }
}
