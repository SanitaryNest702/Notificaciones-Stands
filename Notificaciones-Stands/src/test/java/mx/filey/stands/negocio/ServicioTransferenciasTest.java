package mx.filey.stands.negocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.filey.stands.EscenarioPruebas;
import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Reservacion;

class ServicioTransferenciasTest {
    private EscenarioPruebas sistema;

    @BeforeEach
    void preparar() {
        sistema = new EscenarioPruebas(LocalDate.of(2027, 1, 1));
    }

    @Test
    void unStandBloqueadoSeTransfiereComoReservadoALaEditorial() {
        sistema.servicioStands.bloquear("S-1");

        Reservacion r = sistema.servicioTransferencias.transferir("S-1", "E-2");

        assertEquals(EstadoStand.RESERVADO, sistema.servicioStands.obtener("S-1").getEstado());
        assertEquals("E-2", r.getEditorial().getId());
    }

    @Test
    void laTransferenciaFijaLaVigenciaDesdeElDiaDeLaTransferencia() {
        sistema.servicioStands.bloquear("S-1");
        sistema.reloj.avanzarDias(4);

        Reservacion r = sistema.servicioTransferencias.transferir("S-1", "E-1");

        assertEquals(LocalDate.of(2027, 1, 15), r.getFechaVigencia());
    }

    @Test
    void despuesDeTransferirLaEditorialPuedePagarYOcuparElStand() {
        sistema.servicioStands.bloquear("S-1");
        Reservacion r = sistema.servicioTransferencias.transferir("S-1", "E-1");

        sistema.servicioReservaciones.registrarPago(r.getId(), new BigDecimal("3000"));

        assertEquals(EstadoStand.OCUPADO, sistema.servicioStands.obtener("S-1").getEstado());
    }

    @Test
    void soloSeTransfierenStandsBloqueados() {
        assertThrows(NegocioException.class,
            () -> sistema.servicioTransferencias.transferir("S-1", "E-1"));
    }

    @Test
    void noSeTransfiereUnStandReservadoNiOcupado() {
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        assertThrows(NegocioException.class,
            () -> sistema.servicioTransferencias.transferir("S-1", "E-2"));

        Reservacion r = sistema.servicioReservaciones.reservar("S-2", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), new BigDecimal("3000"));
        assertThrows(NegocioException.class,
            () -> sistema.servicioTransferencias.transferir("S-2", "E-2"));
    }

    @Test
    void transferirUnStandInexistenteFalla() {
        assertThrows(NegocioException.class,
            () -> sistema.servicioTransferencias.transferir("X-9", "E-1"));
    }

    @Test
    void transferirAUnaEditorialInexistenteFallaYElStandSigueBloqueado() {
        sistema.servicioStands.bloquear("S-1");

        assertThrows(NegocioException.class,
            () -> sistema.servicioTransferencias.transferir("S-1", "E-99"));

        assertEquals(EstadoStand.BLOQUEADO, sistema.servicioStands.obtener("S-1").getEstado());
        assertEquals(0, sistema.servicioReservaciones.listarActivas().size());
    }

    @Test
    void unStandBloqueadoNoPuedeReservarseDirectamente() {
        sistema.servicioStands.bloquear("S-1");
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.reservar("S-1", "E-1"));
    }
}
