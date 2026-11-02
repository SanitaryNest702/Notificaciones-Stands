package mx.filey.stands.notificaciones;

/**
 * Canal por el que se entrega un aviso (consola, correo, mensaje...).
 * El resto del sistema solo conoce esta interfaz, no el canal concreto.
 */
public interface Notificador {

    void enviar(Notificacion notificacion);
}
