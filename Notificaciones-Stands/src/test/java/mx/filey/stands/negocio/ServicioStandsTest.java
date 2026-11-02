package mx.filey.stands.negocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.filey.stands.EscenarioPruebas;
import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Stand;

class ServicioStandsTest {
    private EscenarioPruebas sistema;

    @BeforeEach
    void preparar() {
        sistema = new EscenarioPruebas(LocalDate.of(2027, 1, 1));
    }

    // ---- RF-1.1 / RF-1.2 ----

    @Test
    void unStandNuevoNaceDisponible() {
        Stand stand = sistema.servicioStands.crearStand("S-3", 12);
        assertEquals(EstadoStand.DISPONIBLE, stand.getEstado());
        assertEquals("S-3", stand.getId());
        assertEquals(12.0, stand.getMetrosCuadrados());
    }

    @Test
    void noSePermitenIdentificadoresRepetidos() {
        assertThrows(NegocioException.class, () -> sistema.servicioStands.crearStand("S-1", 5));
        assertEquals(2, sistema.servicioStands.listar().size());
    }

    @Test
    void noSePermiteUnIdentificadorVacioONulo() {
        assertThrows(NegocioException.class, () -> sistema.servicioStands.crearStand("", 5));
        assertThrows(NegocioException.class, () -> sistema.servicioStands.crearStand("   ", 5));
        assertThrows(NegocioException.class, () -> sistema.servicioStands.crearStand(null, 5));
        assertEquals(2, sistema.servicioStands.listar().size());
    }

    @Test
    void noSePermitenMetrosCuadradosEnCeroONegativos() {
        assertThrows(NegocioException.class, () -> sistema.servicioStands.crearStand("S-3", 0));
        assertThrows(NegocioException.class, () -> sistema.servicioStands.crearStand("S-3", -4));
        assertEquals(2, sistema.servicioStands.listar().size());
    }

    @Test
    void elPrecioDelStandSeCalculaConSusDimensiones() {
        Stand stand = sistema.servicioStands.crearStand("S-3", 12);
        assertEquals(new BigDecimal("12000.00"), stand.getPrecio());
    }

    @Test
    void elMapaListaLosStandsEnElOrdenDeAlta() {
        sistema.servicioStands.crearStand("S-3", 12);
        assertEquals("S-1", sistema.servicioStands.listar().get(0).getId());
        assertEquals("S-3", sistema.servicioStands.listar().get(2).getId());
    }

    @Test
    void consultarUnStandInexistenteFalla() {
        NegocioException e = assertThrows(NegocioException.class, () -> sistema.servicioStands.obtener("X-9"));
        assertTrue(e.getMessage().contains("X-9"));
    }

    // ---- RF-3.1 ----

    @Test
    void bloquearCambiaElEstadoABloqueado() {
        sistema.servicioStands.bloquear("S-1");
        assertEquals(EstadoStand.BLOQUEADO, sistema.servicioStands.obtener("S-1").getEstado());
    }

    @Test
    void noSePuedeBloquearUnStandYaBloqueado() {
        sistema.servicioStands.bloquear("S-1");
        assertThrows(NegocioException.class, () -> sistema.servicioStands.bloquear("S-1"));
    }

    @Test
    void noSePuedeBloquearUnStandReservado() {
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        assertThrows(NegocioException.class, () -> sistema.servicioStands.bloquear("S-1"));
        assertEquals(EstadoStand.RESERVADO, sistema.servicioStands.obtener("S-1").getEstado());
    }

    @Test
    void unStandOcupadoNoPuedeCambiarDeEstado() {
        var r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), new BigDecimal("3000"));

        assertEquals(EstadoStand.OCUPADO, sistema.servicioStands.obtener("S-1").getEstado());
        assertThrows(NegocioException.class, () -> sistema.servicioStands.bloquear("S-1"));
        assertEquals(EstadoStand.OCUPADO, sistema.servicioStands.obtener("S-1").getEstado());
    }

    @Test
    void bloquearUnStandInexistenteFalla() {
        assertThrows(NegocioException.class, () -> sistema.servicioStands.bloquear("X-9"));
    }

    @Test
    void elAdministradorPuedeLiberarUnBloqueo() {
        sistema.servicioStands.bloquear("S-1");
        sistema.servicioStands.liberarBloqueo("S-1");
        assertEquals(EstadoStand.DISPONIBLE, sistema.servicioStands.obtener("S-1").getEstado());
    }

    @Test
    void noSePuedeLiberarUnStandQueNoEstaBloqueado() {
        assertThrows(NegocioException.class, () -> sistema.servicioStands.liberarBloqueo("S-1"));
        assertEquals(EstadoStand.DISPONIBLE, sistema.servicioStands.obtener("S-1").getEstado());
    }
}
