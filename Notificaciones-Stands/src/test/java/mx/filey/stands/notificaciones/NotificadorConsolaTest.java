package mx.filey.stands.notificaciones;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;

class NotificadorConsolaTest {

    @Test
    void imprimeElAvisoEnLaLineaDeComandos() {
        Reservacion reservacion = new Reservacion("R-1",
            new Stand("S-1", 10, new BigDecimal("10000")),
            new Editorial("E-1", "Editorial Uno", "uno@example.com"),
            LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 11));
        Notificacion aviso = new Notificacion(TipoNotificacion.RESERVA_POR_VENCER,
            reservacion, LocalDate.of(2027, 1, 11), 4);

        PrintStream original = System.out;
        ByteArrayOutputStream captura = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captura, true));
        try {
            new NotificadorConsola().enviar(aviso);
        } finally {
            System.setOut(original);
        }

        String salida = captura.toString();
        assertTrue(salida.startsWith("[AVISO] "));
        assertTrue(salida.contains("Editorial Uno"));
        assertTrue(salida.contains("S-1"));
    }
}
