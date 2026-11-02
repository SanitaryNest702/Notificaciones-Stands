package mx.filey.stands.notificaciones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import mx.filey.stands.EscenarioPruebas;
import mx.filey.stands.datos.Reservacion;

class ServicioNotificacionesTest {

    private static EscenarioPruebas sistemaEn(int anio, int mes, int dia) {
        return new EscenarioPruebas(LocalDate.of(anio, mes, dia));
    }

    // ---- RF-5.1 reserva por vencer ----

    @Test
    void avisaCuandoLaReservaEstaPorVencer() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");   // vigencia: 2027-01-11

        sistema.reloj.avanzarDias(6);                            // faltan 4 dias
        List<Notificacion> avisos = sistema.servicioNotificaciones.revisarVencimientos();

        assertEquals(1, avisos.size());
        assertEquals(TipoNotificacion.RESERVA_POR_VENCER, avisos.get(0).getTipo());
        assertEquals(4, avisos.get(0).getDiasRestantes());
        assertEquals(LocalDate.of(2027, 1, 11), avisos.get(0).getFechaVencimiento());
        assertEquals("E-1", avisos.get(0).getEditorial().getId());
    }

    @Test
    void noAvisaSiTodaviaFaltaMuchoParaVencer() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");

        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().isEmpty());
    }

    @Test
    void elLimiteDeLaVentanaDeAvisoEsInclusivo() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");   // vigencia: 2027-01-11

        sistema.reloj.avanzarDias(4);                            // faltan 6 dias: fuera de la ventana
        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().isEmpty());

        sistema.reloj.avanzarDias(1);                            // faltan 5 dias: justo en el limite
        List<Notificacion> avisos = sistema.servicioNotificaciones.revisarVencimientos();
        assertEquals(1, avisos.size());
        assertEquals(5, avisos.get(0).getDiasRestantes());
    }

    @Test
    void avisaElMismoDiaDelVencimiento() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(10);                           // 2027-01-11

        List<Notificacion> avisos = sistema.servicioNotificaciones.revisarVencimientos();

        assertEquals(1, avisos.size());
        assertEquals(0, avisos.get(0).getDiasRestantes());
    }

    @Test
    void noAvisaDeUnaReservaQueYaVencio() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(11);                           // 2027-01-12

        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().isEmpty());
    }

    @Test
    void noAvisaDeLaReservaSiElStandYaEstaOcupado() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), new BigDecimal("3000"));
        sistema.reloj.avanzarDias(7);

        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().stream()
            .noneMatch(n -> n.getTipo() == TipoNotificacion.RESERVA_POR_VENCER));
    }

    @Test
    void noAvisaDeUnaReservacionCancelada() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(11);
        sistema.servicioReservaciones.liberarReservasVencidas();
        sistema.reloj.avanzarDias(15);   // 2027-01-27: el pronto pago estaria cerca

        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().isEmpty());
    }

    // ---- RF-5.2 pronto pago por vencer ----

    @Test
    void avisaCuandoElProntoPagoEstaPorVencer() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 28);
        sistema.servicioReservaciones.reservar("S-1", "E-1");   // vigencia: 2027-02-07

        List<Notificacion> avisos = sistema.servicioNotificaciones.revisarVencimientos();

        assertEquals(1, avisos.size());
        assertEquals(TipoNotificacion.PRONTO_PAGO_POR_VENCER, avisos.get(0).getTipo());
        assertEquals(LocalDate.of(2027, 1, 31), avisos.get(0).getFechaVencimiento());
        assertEquals(3, avisos.get(0).getDiasRestantes());
    }

    @Test
    void elLimiteDeLaVentanaDelProntoPagoEsInclusivo() {
        EscenarioPruebas fuera = sistemaEn(2027, 1, 25);         // faltan 6 dias
        fuera.servicioReservaciones.reservar("S-1", "E-1");
        assertTrue(fuera.servicioNotificaciones.revisarVencimientos().isEmpty());

        EscenarioPruebas limite = sistemaEn(2027, 1, 26);        // faltan 5 dias
        limite.servicioReservaciones.reservar("S-1", "E-1");
        assertEquals(1, limite.servicioNotificaciones.revisarVencimientos().size());
    }

    @Test
    void noAvisaDelProntoPagoSiLaEditorialYaPago() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 28);
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), new BigDecimal("3000"));

        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().stream()
            .noneMatch(n -> n.getTipo() == TipoNotificacion.PRONTO_PAGO_POR_VENCER));
    }

    @Test
    void noAvisaDelProntoPagoDespuesDeQueVencio() {
        EscenarioPruebas sistema = sistemaEn(2027, 2, 1);        // un dia despues del limite
        sistema.servicioReservaciones.reservar("S-1", "E-1");

        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().stream()
            .noneMatch(n -> n.getTipo() == TipoNotificacion.PRONTO_PAGO_POR_VENCER));
    }

    // ---- RF-5.3 fecha límite de pago por vencer ----

    @Test
    void avisaCuandoLaFechaLimiteDePagoEstaPorVencer() {
        EscenarioPruebas sistema = sistemaEn(2027, 2, 12);
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), new BigDecimal("3000"));   // Ocupado

        List<Notificacion> avisos = sistema.servicioNotificaciones.revisarVencimientos();

        assertEquals(1, avisos.size());
        assertEquals(TipoNotificacion.FECHA_LIMITE_POR_VENCER, avisos.get(0).getTipo());
        assertEquals(LocalDate.of(2027, 2, 15), avisos.get(0).getFechaVencimiento());
        assertEquals(3, avisos.get(0).getDiasRestantes());
    }

    @Test
    void laFechaLimiteDePagoTambienAvisaAUnaReservaSinPagos() {
        EscenarioPruebas sistema = sistemaEn(2027, 2, 10);       // faltan 5 dias; la reserva vence en 10
        sistema.servicioReservaciones.reservar("S-1", "E-1");

        List<Notificacion> avisos = sistema.servicioNotificaciones.revisarVencimientos();

        assertEquals(1, avisos.size());
        assertEquals(TipoNotificacion.FECHA_LIMITE_POR_VENCER, avisos.get(0).getTipo());
    }

    @Test
    void noAvisaSiElStandYaEstaPagadoPorCompleto() {
        EscenarioPruebas sistema = sistemaEn(2027, 2, 12);
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), new BigDecimal("10000"));

        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().isEmpty());
    }

    @Test
    void noAvisaDeLaFechaLimiteDespuesDeQueVencio() {
        EscenarioPruebas sistema = sistemaEn(2027, 2, 16);
        Reservacion r = sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.registrarPago(r.getId(), new BigDecimal("3000"));

        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().isEmpty());
    }

    // ---- reglas comunes: sin repetir, varios avisos, varias editoriales ----

    @Test
    void noRepiteElMismoAviso() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(6);

        sistema.servicioNotificaciones.revisarVencimientos();
        List<Notificacion> segunda = sistema.servicioNotificaciones.revisarVencimientos();

        assertTrue(segunda.isEmpty());
        assertEquals(1, sistema.avisosEntregados.size());
    }

    @Test
    void unNuevoDiaTampocoRepiteUnAvisoYaEnviado() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(6);
        sistema.servicioNotificaciones.revisarVencimientos();

        sistema.reloj.avanzarDias(1);

        assertTrue(sistema.servicioNotificaciones.revisarVencimientos().isEmpty());
        assertEquals(1, sistema.avisosEntregados.size());
    }

    @Test
    void puedeEnviarDosTiposDeAvisoALaVez() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 22);
        sistema.servicioReservaciones.reservar("S-1", "E-1");   // vigencia: 2027-02-01
        sistema.reloj.avanzarDias(5);                            // 2027-01-27

        List<Notificacion> avisos = sistema.servicioNotificaciones.revisarVencimientos();

        assertEquals(2, avisos.size());
        assertEquals(TipoNotificacion.RESERVA_POR_VENCER, avisos.get(0).getTipo());
        assertEquals(TipoNotificacion.PRONTO_PAGO_POR_VENCER, avisos.get(1).getTipo());
    }

    @Test
    void cadaEditorialRecibeSusPropiosAvisos() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.reservar("S-2", "E-2");
        sistema.reloj.avanzarDias(6);

        List<Notificacion> avisos = sistema.servicioNotificaciones.revisarVencimientos();

        assertEquals(2, avisos.size());
        assertEquals("E-1", avisos.get(0).getEditorial().getId());
        assertEquals("E-2", avisos.get(1).getEditorial().getId());
    }

    @Test
    void cadaAvisoSeEntregaAlCanalConfigurado() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(6);

        List<Notificacion> devueltos = sistema.servicioNotificaciones.revisarVencimientos();

        assertEquals(devueltos, sistema.avisosEntregados);
    }

    @Test
    void conCeroDiasDeAvisoSoloAvisaElMismoDia() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        var config = new mx.filey.stands.configuracion.Configuracion(
            new BigDecimal("1000"), 10, 30, 10,
            LocalDate.of(2027, 1, 31), LocalDate.of(2027, 2, 15), 0);
        List<Notificacion> entregados = new ArrayList<>();
        ServicioNotificaciones servicio = new ServicioNotificaciones(
            sistema.reservaciones, entregados::add, config, sistema.reloj);
        sistema.servicioReservaciones.reservar("S-1", "E-1");   // vigencia: 2027-01-11

        sistema.reloj.avanzarDias(9);
        assertTrue(servicio.revisarVencimientos().isEmpty());
        sistema.reloj.avanzarDias(1);
        assertEquals(1, servicio.revisarVencimientos().size());
    }

    // ---- excepciones: falla del canal de entrega ----

    @Test
    void siElCanalFallaElAvisoNoSeDaPorEnviadoYSeReintenta() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.reloj.avanzarDias(6);

        boolean[] canalCaido = {true};
        List<Notificacion> entregados = new ArrayList<>();
        ServicioNotificaciones servicio = new ServicioNotificaciones(sistema.reservaciones, n -> {
            if (canalCaido[0]) {
                throw new IllegalStateException("sin conexion");
            }
            entregados.add(n);
        }, sistema.configuracion, sistema.reloj);

        assertTrue(servicio.revisarVencimientos().isEmpty());
        assertEquals(1, servicio.getFallosDeEnvio());
        assertTrue(entregados.isEmpty());

        canalCaido[0] = false;
        assertEquals(1, servicio.revisarVencimientos().size());
        assertEquals(1, entregados.size());
    }

    @Test
    void laFallaConUnaEditorialNoBloqueaLosAvisosDeLasDemas() {
        EscenarioPruebas sistema = sistemaEn(2027, 1, 1);
        sistema.servicioReservaciones.reservar("S-1", "E-1");
        sistema.servicioReservaciones.reservar("S-2", "E-2");
        sistema.reloj.avanzarDias(6);

        List<Notificacion> entregados = new ArrayList<>();
        ServicioNotificaciones servicio = new ServicioNotificaciones(sistema.reservaciones, n -> {
            if (n.getEditorial().getId().equals("E-1")) {
                throw new IllegalStateException("correo rechazado");
            }
            entregados.add(n);
        }, sistema.configuracion, sistema.reloj);

        List<Notificacion> enviadas = servicio.revisarVencimientos();

        assertEquals(1, enviadas.size());
        assertEquals("E-2", enviadas.get(0).getEditorial().getId());
        assertEquals(1, servicio.getFallosDeEnvio());
    }
}
