package mx.filey.stands.notificaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;

class NotificacionTest {
    private final Reservacion reservacion = new Reservacion("R-7",
        new Stand("S-5", 10, new BigDecimal("10000")),
        new Editorial("E-3", "Libros del Sur", "sur@example.com"),
        LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 11));

    @Test
    void laClaveCombinaReservacionYTipo() {
        Notificacion n = new Notificacion(TipoNotificacion.RESERVA_POR_VENCER,
            reservacion, LocalDate.of(2027, 1, 11), 4);
        assertEquals("R-7:RESERVA_POR_VENCER", n.getClave());
    }

    @Test
    void tiposDistintosTienenClavesDistintas() {
        Notificacion a = new Notificacion(TipoNotificacion.RESERVA_POR_VENCER,
            reservacion, LocalDate.of(2027, 1, 11), 4);
        Notificacion b = new Notificacion(TipoNotificacion.PRONTO_PAGO_POR_VENCER,
            reservacion, LocalDate.of(2027, 1, 31), 4);
        assertTrue(!a.getClave().equals(b.getClave()));
    }

    @Test
    void elMensajeIdentificaAEditorialStandFechaYDias() {
        Notificacion n = new Notificacion(TipoNotificacion.PRONTO_PAGO_POR_VENCER,
            reservacion, LocalDate.of(2027, 1, 31), 3);
        String mensaje = n.getMensaje();

        assertTrue(mensaje.contains("Libros del Sur"));
        assertTrue(mensaje.contains("sur@example.com"));
        assertTrue(mensaje.contains("S-5"));
        assertTrue(mensaje.contains("2027-01-31"));
        assertTrue(mensaje.contains("3 dias"));
        assertTrue(mensaje.contains("pronto pago"));
        assertEquals(mensaje, n.toString());
    }

    @Test
    void cadaTipoTieneUnaDescripcion() {
        for (TipoNotificacion t : TipoNotificacion.values()) {
            assertTrue(!t.getDescripcion().isBlank());
        }
        assertEquals(3, TipoNotificacion.values().length);
    }
}
