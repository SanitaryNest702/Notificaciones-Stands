package mx.filey.stands.negocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class RelojSimuladoTest {

    @Test
    void arrancaEnLaFechaIndicada() {
        assertEquals(LocalDate.of(2027, 1, 1), new RelojSimulado(LocalDate.of(2027, 1, 1)).hoy());
    }

    @Test
    void sinParametrosArrancaHoy() {
        assertEquals(LocalDate.now(), new RelojSimulado().hoy());
    }

    @Test
    void avanzarSumaLosDiasIncluyendoElCambioDeMes() {
        RelojSimulado r = new RelojSimulado(LocalDate.of(2027, 1, 30));
        r.avanzarDias(3);
        assertEquals(LocalDate.of(2027, 2, 2), r.hoy());
    }

    @Test
    void avanzarCeroDiasNoCambiaLaFecha() {
        RelojSimulado r = new RelojSimulado(LocalDate.of(2027, 1, 1));
        r.avanzarDias(0);
        assertEquals(LocalDate.of(2027, 1, 1), r.hoy());
    }

    @Test
    void noSePuedeRetrocederElReloj() {
        RelojSimulado r = new RelojSimulado(LocalDate.of(2027, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> r.avanzarDias(-1));
        assertEquals(LocalDate.of(2027, 1, 1), r.hoy());
    }
}
