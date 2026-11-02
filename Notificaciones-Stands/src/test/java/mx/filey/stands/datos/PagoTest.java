package mx.filey.stands.datos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class PagoTest {

    @Test
    void guardaElMontoYLaFecha() {
        Pago p = new Pago(new BigDecimal("2500.50"), LocalDate.of(2027, 1, 5));
        assertEquals(new BigDecimal("2500.50"), p.getMonto());
        assertEquals(LocalDate.of(2027, 1, 5), p.getFecha());
    }

    @Test
    void rechazaMontosNulosEnCeroONegativos() {
        LocalDate f = LocalDate.of(2027, 1, 5);
        assertThrows(IllegalArgumentException.class, () -> new Pago(null, f));
        assertThrows(IllegalArgumentException.class, () -> new Pago(BigDecimal.ZERO, f));
        assertThrows(IllegalArgumentException.class, () -> new Pago(new BigDecimal("-1"), f));
    }

    @Test
    void rechazaUnaFechaNula() {
        assertThrows(IllegalArgumentException.class, () -> new Pago(BigDecimal.TEN, null));
    }
}
