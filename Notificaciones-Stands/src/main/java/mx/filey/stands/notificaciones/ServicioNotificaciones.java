package mx.filey.stands.notificaciones;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mx.filey.stands.configuracion.Configuracion;
import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Repositorio;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.negocio.Reloj;

/**
 * Detecta reservaciones con vencimientos cercanos y envía los avisos
 * a través del {@link Notificador}. Cada aviso se envía una sola vez.
 */
public class ServicioNotificaciones {
    private final Repositorio<Reservacion> reservaciones;
    private final Notificador notificador;
    private final Configuracion configuracion;
    private final Reloj reloj;
    private final Set<String> enviadas = new HashSet<>();
    private int fallosDeEnvio = 0;

    public ServicioNotificaciones(Repositorio<Reservacion> reservaciones,
                                  Notificador notificador,
                                  Configuracion configuracion,
                                  Reloj reloj) {
        this.reservaciones = reservaciones;
        this.notificador = notificador;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    /**
     * Revisa todas las reservaciones activas y envía los avisos que correspondan.
     *
     * @return los avisos enviados en esta revisión (vacía si no hay ninguno nuevo)
     */
    public List<Notificacion> revisarVencimientos() {
        LocalDate hoy = reloj.hoy();
        List<Notificacion> enviadasAhora = new ArrayList<>();

        for (Reservacion r : reservaciones.listar()) {
            if (!r.isActiva()) {
                continue;
            }

            if (r.getStand().getEstado() == EstadoStand.RESERVADO) {
                intentarAvisar(TipoNotificacion.RESERVA_POR_VENCER, r,
                    r.getFechaVigencia(), hoy, enviadasAhora);
            }
            if (r.getMontoPagado().signum() == 0) {
                intentarAvisar(TipoNotificacion.PRONTO_PAGO_POR_VENCER, r,
                    configuracion.getFechaLimiteProntoPago(), hoy, enviadasAhora);
            }
            if (!r.estaPagadaCompleta()) {
                intentarAvisar(TipoNotificacion.FECHA_LIMITE_POR_VENCER, r,
                    configuracion.getFechaMaximaPago(), hoy, enviadasAhora);
            }
        }
        return enviadasAhora;
    }

    private void intentarAvisar(TipoNotificacion tipo, Reservacion reservacion,
                                LocalDate vencimiento, LocalDate hoy,
                                List<Notificacion> acumulador) {
        long dias = ChronoUnit.DAYS.between(hoy, vencimiento);
        boolean dentroDeLaVentana = dias >= 0 && dias <= configuracion.getDiasAvisoVencimiento();
        if (!dentroDeLaVentana) {
            return;
        }

        Notificacion notificacion = new Notificacion(tipo, reservacion, vencimiento, dias);
        if (enviadas.contains(notificacion.getClave())) {
            return;
        }
        try {
            notificador.enviar(notificacion);
        } catch (RuntimeException e) {
            // El canal falló: el aviso NO se marca como enviado, así se reintenta
            // en la siguiente revisión, y los avisos de otras editoriales no se bloquean.
            fallosDeEnvio++;
            return;
        }
        enviadas.add(notificacion.getClave());
        acumulador.add(notificacion);
    }

    /** Cantidad de avisos que el canal no pudo entregar (quedan pendientes de reintento). */
    public int getFallosDeEnvio() {
        return fallosDeEnvio;
    }
}
