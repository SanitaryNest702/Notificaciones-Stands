package mx.filey.stands.app;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

/**
 * Prueba la línea de comandos de punta a punta (RNF-1): se le dan las opciones
 * del menú como texto y se revisa lo que imprime. La consola solo consulta, avanza la fecha
 * y revisa vencimientos; los datos salen de la precarga de {@code DatosDemo}.
 */
class MainTest {

    private static String ejecutar(String... lineas) {
        String entrada = String.join("\n", lineas) + "\n0\n";
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captura = new ByteArrayOutputStream();
        try {
            System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
            System.setOut(new PrintStream(captura, true, StandardCharsets.UTF_8));
            new Main().ejecutar();
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
        return captura.toString(StandardCharsets.UTF_8);
    }

    @Test
    void elMenuMuestraLasOpcionesYSeCierraConCero() {
        String salida = ejecutar();
        assertTrue(salida.contains("Sistema administrativo de stands FILEY"));
        assertTrue(salida.contains("Revisar vencimientos y enviar avisos"));
        assertTrue(salida.contains("Hasta luego."));
    }

    @Test
    void elMenuNoOfreceOpcionesDeCreacionNiEdicion() {
        String salida = ejecutar();
        assertFalse(salida.contains("Reservar un stand"));
        assertFalse(salida.contains("Registrar un pago"));
        assertFalse(salida.contains("Bloquear"));
        assertFalse(salida.contains("Transferir"));
    }

    @Test
    void alIniciarSePrecargaLaDemostracion() {
        String salida = ejecutar();
        assertTrue(salida.contains("8 stands y 5 reservaciones"));
        assertTrue(salida.contains("Pronto pago ("));
    }

    @Test
    void elMapaMuestraLosOchoStandsDeLaDemostracion() {
        String salida = ejecutar("1");
        assertTrue(salida.contains("A-01"));
        assertTrue(salida.contains("B-04"));
        assertTrue(salida.contains("DISPONIBLE"));
        assertTrue(salida.contains("BLOQUEADO"));
    }

    @Test
    void laOpcionUnoListaLasReservacionesPrecargadasConSusDetalles() {
        String salida = ejecutar("1");
        assertTrue(salida.contains("R-1 | stand A-01"));
        assertTrue(salida.contains("R-5 | stand B-02"));
        assertTrue(salida.contains("Vigencia:"));
        assertTrue(salida.contains("Monto pagado:"));
        assertTrue(salida.contains("OCUPADO"));
    }

    @Test
    void laPrimeraRevisionAvisaDeLaReservaProximaAVencer() {
        String salida = ejecutar("3");
        assertTrue(salida.contains("[AVISO]"));
        assertTrue(salida.contains("Editorial Horizonte"));
    }

    @Test
    void unNumeroDeDiasQueNoEsNumeroSeRechazaConUnMensaje() {
        assertTrue(ejecutar("2", "abc").contains("Dato numerico invalido."));
    }

    @Test
    void avanzarUnNumeroNegativoDeDiasSeRechaza() {
        String salida = ejecutar("2", "-3");
        assertTrue(salida.contains("Dato invalido:"));
    }

    @Test
    void unaOpcionInexistenteSeIgnoraConUnAviso() {
        assertTrue(ejecutar("99").contains("Opcion no valida."));
        assertTrue(ejecutar("4").contains("Opcion no valida."));
    }

    @Test
    void alAvanzarLosDiasSeAvisaYSeLiberanAutomaticamenteLasReservasSinPago() {
        String salida = ejecutar("2", "20");
        assertTrue(salida.contains("[AVISO]"));
        assertTrue(salida.contains("[VENCIDA] R-1"));
        assertTrue(salida.contains("volvio a Disponible"));
    }

    @Test
    void cadaReservaVenceConSuPropiaVigencia() {
        String salida = ejecutar("2", "2");   // R-1 vence primero; R-2 y las demas siguen activas
        assertTrue(salida.contains("[VENCIDA] R-1"));
        assertFalse(salida.contains("[VENCIDA] R-2"));
        assertFalse(salida.contains("[VENCIDA] R-4"));
    }

    @Test
    void unStandOcupadoNoSeLiberaAlAvanzar() {
        String salida = ejecutar("2", "20");
        assertFalse(salida.contains("[VENCIDA] R-3"));
    }

    @Test
    void sinNovedadesLaRevisionLoDice() {
        assertTrue(ejecutar("2", "20", "3").contains("No hay avisos nuevos."));
    }
}
