package mx.filey.stands.notificaciones;

import java.time.LocalDate;

import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Reservacion;

/**
 * Aviso dirigido a una editorial sobre un vencimiento cercano.
 */
public class Notificacion {
    private final TipoNotificacion tipo;
    private final Reservacion reservacion;
    private final LocalDate fechaVencimiento;
    private final long diasRestantes;

    public Notificacion(TipoNotificacion tipo, Reservacion reservacion,
                        LocalDate fechaVencimiento, long diasRestantes) {
        this.tipo = tipo;
        this.reservacion = reservacion;
        this.fechaVencimiento = fechaVencimiento;
        this.diasRestantes = diasRestantes;
    }

    public TipoNotificacion getTipo() {
        return tipo;
    }

    public Editorial getEditorial() {
        return reservacion.getEditorial();
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public long getDiasRestantes() {
        return diasRestantes;
    }

    /** Identifica el aviso para no enviarlo dos veces: reservación + tipo. */
    public String getClave() {
        return reservacion.getId() + ":" + tipo;
    }

    public String getMensaje() {
        return "Para " + getEditorial().getNombre() + " (" + getEditorial().getCorreo() + "): "
            + tipo.getDescripcion() + " del stand " + reservacion.getStand().getId()
            + " vence el " + fechaVencimiento + " (en " + diasRestantes + " dias).";
    }

    @Override
    public String toString() {
        return getMensaje();
    }
}
