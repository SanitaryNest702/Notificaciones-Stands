package mx.filey.stands.datos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservacionTest {
    private static final LocalDate HOY = LocalDate.of(2027, 1, 1);
    private Reservacion reservacion;

    @BeforeEach
    void preparar() {
        Stand stand = new Stand("S-1", 10, new BigDecimal("10000.00"));
        Editorial editorial = new Editorial("E-1", "Editorial Uno", "uno@example.com");
        reservacion = new Reservacion("R-1", stand, editorial, HOY, HOY.plusDays(10));
    }

    @Test
    void naceActivaYSinPagos() {
        assertTrue(reservacion.isActiva());
        assertTrue(reservacion.getPagos().isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(reservacion.getMontoPagado()));
        assertEquals(0.0, reservacion.getPorcentajePagado());
        assertFalse(reservacion.estaPagadaCompleta());
    }

    @Test
    void losPagosSeAcumulanYSeConservanEnElHistorial() {
        reservacion.registrarPago(new Pago(new BigDecimal("1000"), HOY));
        reservacion.registrarPago(new Pago(new BigDecimal("2500"), HOY.plusDays(3)));

        assertEquals(2, reservacion.getPagos().size());
        assertEquals(0, new BigDecimal("3500").compareTo(reservacion.getMontoPagado()));
        assertEquals(35.0, reservacion.getPorcentajePagado());
        assertEquals(0, new BigDecimal("6500.00").compareTo(reservacion.getSaldoPendiente()));
    }

    @Test
    void estaPagadaCompletaAlCubrirElCosto() {
        reservacion.registrarPago(new Pago(new BigDecimal("10000"), HOY));
        assertTrue(reservacion.estaPagadaCompleta());
        assertEquals(0, BigDecimal.ZERO.compareTo(reservacion.getSaldoPendiente()));
    }

    @Test
    void haCubiertoAlMenosComparaMontosExactosSinRedondear() {
        reservacion.registrarPago(new Pago(new BigDecimal("2999.99"), HOY));
        assertFalse(reservacion.haCubiertoAlMenos(30));   // 29.9999%, aunque se muestre como 30.0
        assertEquals(30.0, reservacion.getPorcentajePagado());

        reservacion.registrarPago(new Pago(new BigDecimal("0.01"), HOY));
        assertTrue(reservacion.haCubiertoAlMenos(30));
    }

    @Test
    void asegurarElDescuentoCambiaElCostoAplicable() {
        reservacion.asegurarDescuento(new BigDecimal("9000.00"));
        assertTrue(reservacion.isDescuentoAsegurado());
        assertEquals(new BigDecimal("9000.00"), reservacion.getCostoAplicable());
    }

    @Test
    void elHistorialEsDeSoloLectura() {
        assertThrows(UnsupportedOperationException.class,
            () -> reservacion.getPagos().add(new Pago(BigDecimal.TEN, HOY)));
    }

    @Test
    void rechazaUnPagoNulo() {
        assertThrows(IllegalArgumentException.class, () -> reservacion.registrarPago(null));
    }

    @Test
    void cancelarLaDejaInactiva() {
        reservacion.cancelar();
        assertFalse(reservacion.isActiva());
    }

    @Test
    void rechazaDatosInvalidosAlCrearse() {
        Stand stand = new Stand("S-2", 10, BigDecimal.TEN);
        Editorial editorial = new Editorial("E-1", "Uno", "uno@example.com");
        assertThrows(IllegalArgumentException.class,
            () -> new Reservacion("R-2", null, editorial, HOY, HOY.plusDays(1)));
        assertThrows(IllegalArgumentException.class,
            () -> new Reservacion("R-2", stand, null, HOY, HOY.plusDays(1)));
        assertThrows(IllegalArgumentException.class,
            () -> new Reservacion("R-2", stand, editorial, HOY, HOY.minusDays(1)));
    }
}
