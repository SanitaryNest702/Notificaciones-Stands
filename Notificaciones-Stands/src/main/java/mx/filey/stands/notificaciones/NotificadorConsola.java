package mx.filey.stands.notificaciones;

/**
 * Entrega los avisos imprimiéndolos en la línea de comandos.
 */
public class NotificadorConsola implements Notificador {

    @Override
    public void enviar(Notificacion notificacion) {
        System.out.println("[AVISO] " + notificacion.getMensaje());
    }
}
