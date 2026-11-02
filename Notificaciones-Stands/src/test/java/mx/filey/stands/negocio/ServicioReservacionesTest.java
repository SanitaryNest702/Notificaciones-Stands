package mx.filey.stands.negocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mx.filey.stands.EscenarioPruebas;
import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Reservacion;

class ServicioReservacionesTest {
    private EscenarioPruebas sistema;

    @BeforeEach
    void preparar() {
        sistema = new EscenarioPruebas(LocalDate.of(2027, 1, 1));
    }

    private static BigDecimal pesos(String monto) {
        return new BigDecimal(monto);
    }

    private static void assertMonto(String esperado, BigDecimal real) {
        assertEquals(0, new BigDecimal(esperado).compareTo(real), "esperado " + esperado + " pero fue " + real);
    }

    // ---- RF-2.1 reservar ----

    @Test
    void reservarCambiaElEstadoYFijaLaVigencia() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");

        assertEquals(EstadoStand.RESERVADO, sistema.servicioStands.obtener("S-1").getEstado());
        assertEquals(LocalDate.of(2027, 1, 11), r.getFechaVigencia());
        assertEquals("E-1", r.getEditorial().getId());
        assertTrue(r.isActiva());
    }

    @Test
    void cadaReservacionRecibeUnFolioConsecutivo() {
        assertEquals("R-1", sistema.servicioReservaciones.reservar("S-1", "E-1").getId());
        assertEquals("R-2", sistema.servicioReservaciones.reservar("S-2", "E-2").getId());
    }

    @Test
    void noSePuedeReservarUnStandQueYaEstaReservado() {
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.reservar("S-1", "E-2"));
    }

    @Test
    void noSePuedeReservarUnStandOcupado() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("3000"));
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.reservar("S-1", "E-2"));
    }

    @Test
    void reservarUnStandInexistenteFalla() {
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.reservar("X-9", "E-1"));
    }

    @Test
    void reservarConUnaEditorialInexistenteFallaYElStandSigueDisponible() {
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.reservar("S-1", "E-99"));
        assertEquals(EstadoStand.DISPONIBLE, sistema.servicioStands.obtener("S-1").getEstado());
        assertTrue(sistema.servicioReservaciones.listarActivas().isEmpty());
    }

    // ---- RF-2.2 pagos y paso a Ocupado ----

    @Test
    void conElPorcentajeMinimoElStandPasaAOcupado() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");

        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("2000"));
        assertEquals(EstadoStand.RESERVADO, r.getStand().getEstado());   // 22.2% de 9000
        assertEquals(22.22, r.getPorcentajePagado());

        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("700"));
        assertEquals(EstadoStand.OCUPADO, r.getStand().getEstado());     // 30% de 9000
    }

    @Test
    void exactamenteElPorcentajeMinimoYaOcupaElStand() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("2700"));   // 30.00% de 9000

        assertEquals(30.0, r.getPorcentajePagado());
        assertEquals(EstadoStand.OCUPADO, r.getStand().getEstado());
    }

    @Test
    void unCentavoPorDebajoDelMinimoNoOcupaElStand() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("2699.99"));

        assertEquals(EstadoStand.RESERVADO, r.getStand().getEstado());
    }

    @Test
    void sePuedePagarElCostoCompletoDeUnaVez() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("9000"));

        assertTrue(r.estaPagadaCompleta());
        assertEquals(100.0, r.getPorcentajePagado());
        assertEquals(EstadoStand.OCUPADO, r.getStand().getEstado());
    }

    @Test
    void elHistorialGuardaCadaPagoConSuFecha() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("1000"));
        sistema.reloj.avanzarDias(2);
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("500"));

        assertEquals(2, r.getPagos().size());
        assertEquals(LocalDate.of(2027, 1, 3), r.getPagos().get(1).getFecha());
        assertMonto("1500", r.getMontoPagado());
    }

    @Test
    void noSePermiteUnPagoMayorAlSaldo() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.registrarPago(r.getId(), pesos("99999")));
        assertMonto("0", r.getMontoPagado());
        assertFalse(r.isDescuentoAsegurado());   // un pago rechazado no fija el descuento
    }

    @Test
    void unCentavoMasQueElSaldoSeRechaza() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("3000"));

        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.registrarPago(r.getId(), pesos("6000.01")));
        assertMonto("3000", r.getMontoPagado());
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("6000.00"));   // el saldo exacto sí
        assertTrue(r.estaPagadaCompleta());
    }

    @Test
    void noSePermiteUnPagoEnCeroNegativoNiNulo() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");

        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.registrarPago(r.getId(), BigDecimal.ZERO));
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.registrarPago(r.getId(), pesos("-500")));
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.registrarPago(r.getId(), null));
        assertMonto("0", r.getMontoPagado());
        assertFalse(r.isDescuentoAsegurado());
    }

    @Test
    void pagarUnaReservacionInexistenteFalla() {
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.registrarPago("R-99", pesos("100")));
    }

    @Test
    void noSePuedePagarUnaReservaVencida() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(11);
        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.registrarPago(r.getId(), pesos("3000")));
    }

    @Test
    void elUltimoDiaDeVigenciaTodaviaSePuedePagar() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(10);   // 2027-01-11, ultimo dia

        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("3000"));
        assertEquals(EstadoStand.OCUPADO, r.getStand().getEstado());
    }

    @Test
    void noSePuedePagarUnaReservacionCancelada() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(11);
        sistema.servicioReservaciones.liberarReservasVencidas();

        NegocioException e = assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.registrarPago(r.getId(), pesos("100")));
        assertTrue(e.getMessage().contains("no esta activa"));
    }

    @Test
    void unStandOcupadoSigueRecibiendoPagosAunqueYaPasoLaVigencia() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("3000"));
        sistema.reloj.avanzarDias(30);

        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("1000"));
        assertMonto("4000", r.getMontoPagado());
        assertEquals(EstadoStand.OCUPADO, r.getStand().getEstado());
    }

    // ---- RF-4.1 descuento por pronto pago ----

    @Test
    void elPrimerPagoATiempoAplicaDescuento() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("1000"));

        assertTrue(r.isDescuentoAsegurado());
        assertEquals(new BigDecimal("9000.00"), r.getCostoAplicable());
    }

    @Test
    void elPrimerPagoTardeNoAplicaDescuento() {
        sistema.reloj.avanzarDias(31);   // 2027-02-01
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("1000"));

        assertFalse(r.isDescuentoAsegurado());
        assertEquals(new BigDecimal("10000.00"), r.getCostoAplicable());
    }

    @Test
    void elUltimoDiaDelProntoPagoTodaviaAplicaDescuento() {
        sistema.reloj.avanzarDias(30);   // 2027-01-31
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("1000"));

        assertTrue(r.isDescuentoAsegurado());
        assertEquals(new BigDecimal("9000.00"), r.getCostoAplicable());
    }

    @Test
    void elDescuentoAseguradoEnElPrimerPagoSeConservaEnLosSiguientes() {
        sistema.reloj.avanzarDias(29);   // 2027-01-30
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("1000"));   // a tiempo
        sistema.reloj.avanzarDias(5);                                            // 2027-02-04, ya sin descuento
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("2000"));

        assertEquals(new BigDecimal("9000.00"), r.getCostoAplicable());
        assertEquals(EstadoStand.OCUPADO, r.getStand().getEstado());   // 3000 de 9000 = 33.3%
    }

    @Test
    void sinDescuentoElMontoMaximoAPagarEsElPrecioCompleto() {
        sistema.reloj.avanzarDias(31);   // 2027-02-01
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");

        assertThrows(NegocioException.class,
            () -> sistema.servicioReservaciones.registrarPago(r.getId(), pesos("10000.01")));
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("10000"));
        assertTrue(r.estaPagadaCompleta());
    }

    // ---- liberar reservas vencidas ----

    @Test
    void unaReservaVencidaLiberaElStand() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(11);

        assertEquals(1, sistema.servicioReservaciones.liberarReservasVencidas().size());
        assertEquals(EstadoStand.DISPONIBLE, sistema.servicioStands.obtener("S-1").getEstado());
        assertFalse(r.isActiva());
    }

    @Test
    void elUltimoDiaDeVigenciaLaReservaNoSeLibera() {
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(10);

        assertTrue(sistema.servicioReservaciones.liberarReservasVencidas().isEmpty());
        assertEquals(EstadoStand.RESERVADO, sistema.servicioStands.obtener("S-1").getEstado());
    }

    @Test
    void unStandOcupadoNoSeLiberaAunquePaseLaVigencia() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("3000"));
        sistema.reloj.avanzarDias(30);

        assertTrue(sistema.servicioReservaciones.liberarReservasVencidas().isEmpty());
        assertEquals(EstadoStand.OCUPADO, sistema.servicioStands.obtener("S-1").getEstado());
    }

    @Test
    void unaReservaConPagoInsuficienteSeLiberaYConservaSuHistorial() {
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), pesos("1000"));   // 11%, bajo el minimo
        sistema.reloj.avanzarDias(11);

        sistema.servicioReservaciones.liberarReservasVencidas();

        assertEquals(EstadoStand.DISPONIBLE, r.getStand().getEstado());
        assertFalse(r.isActiva());
        assertEquals(1, r.getPagos().size());
    }

    @Test
    void unStandLiberadoPuedeReservarloOtraEditorial() {
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(11);
        sistema.servicioReservaciones.liberarReservasVencidas();

        Reservacion nueva = sistema.servicioReservaciones.reservar("S-1", "E-2");
        assertEquals("E-2", nueva.getEditorial().getId());
        assertEquals(LocalDate.of(2027, 1, 22), nueva.getFechaVigencia());
    }

    @Test
    void liberarDosVecesNoLiberaNadaLaSegundaVez() {
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(11);

        assertEquals(1, sistema.servicioReservaciones.liberarReservasVencidas().size());
        assertTrue(sistema.servicioReservaciones.liberarReservasVencidas().isEmpty());
    }

    @Test
    void listarActivasOmiteLasReservacionesCanceladas() {
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(5);
        sistema.servicioReservaciones.reservar("S-2", "E-2");
        sistema.reloj.avanzarDias(6);   // la primera venció, la segunda no
        sistema.servicioReservaciones.liberarReservasVencidas();

        assertEquals(1, sistema.servicioReservaciones.listarActivas().size());
        assertEquals("S-2", sistema.servicioReservaciones.listarActivas().get(0).getStand().getId());
    }

    @Test
    void obtenerUnaReservacionInexistenteFalla() {
        assertThrows(NegocioException.class, () -> sistema.servicioReservaciones.obtener("R-99"));
    }
}
