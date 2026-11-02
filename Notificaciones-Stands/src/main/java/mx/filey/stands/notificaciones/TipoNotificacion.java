package mx.filey.stands.notificaciones;

/**
 * Tipos de aviso que el sistema envía a las editoriales.
 */
public enum TipoNotificacion {
    RESERVA_POR_VENCER("su reserva"),
    PRONTO_PAGO_POR_VENCER("el descuento por pronto pago"),
    FECHA_LIMITE_POR_VENCER("la fecha limite para cubrir el costo");

    private final String descripcion;

    TipoNotificacion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
