package mx.filey.stands.negocio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import mx.filey.stands.EscenarioPruebas;

class CalculadoraPreciosTest {
    private final CalculadoraPrecios calculadora =
        new CalculadoraPrecios(new EscenarioPruebas(LocalDate.of(2027, 1, 1)).configuracion);

    @Test
    void elPrecioDependeDeLosMetrosCuadrados() {
        assertEquals(new BigDecimal("12000.00"), calculadora.precioDeStand(12));
        assertEquals(new BigDecimal("13500.00"), calculadora.precioDeStand(13.5));
    }

    @Test
    void elPrecioSeRedondeaADosDecimales() {
        assertEquals(new BigDecimal("123.46"), calculadora.precioDeStand(0.123456));
    }

    @Test
    void conDescuentoSiSePagaAntesDeLaFechaLimite() {
        BigDecimal costo = calculadora.costoAplicable(new BigDecimal("10000.00"), LocalDate.of(2027, 1, 10));
        assertEquals(new BigDecimal("9000.00"), costo);
    }

    @Test
    void elUltimoDiaTodaviaTieneDescuento() {
        assertTrue(calculadora.tieneDescuento(LocalDate.of(2027, 1, 31)));
        assertEquals(new BigDecimal("9000.00"),
            calculadora.costoAplicable(new BigDecimal("10000.00"), LocalDate.of(2027, 1, 31)));
    }

    @Test
    void sinDescuentoDespuesDeLaFechaLimite() {
        assertFalse(calculadora.tieneDescuento(LocalDate.of(2027, 2, 1)));
        BigDecimal costo = calculadora.costoAplicable(new BigDecimal("10000.00"), LocalDate.of(2027, 2, 1));
        assertEquals(new BigDecimal("10000.00"), costo);
    }

    @Test
    void unPagoMuyAnteriorTambienTieneDescuento() {
        assertTrue(calculadora.tieneDescuento(LocalDate.of(2026, 6, 1)));
    }
}
