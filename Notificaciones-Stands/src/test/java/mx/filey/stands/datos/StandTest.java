package mx.filey.stands.datos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class StandTest {

    @Test
    void unStandNuevoGuardaSusDatosYNaceDisponible() {
        Stand s = new Stand("A-1", 12.5, new BigDecimal("12500.00"));

        assertEquals("A-1", s.getId());
        assertEquals(12.5, s.getMetrosCuadrados());
        assertEquals(new BigDecimal("12500.00"), s.getPrecio());
        assertEquals(EstadoStand.DISPONIBLE, s.getEstado());
    }

    @Test
    void rechazaUnIdentificadorVacioONulo() {
        assertThrows(IllegalArgumentException.class, () -> new Stand("", 10, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new Stand("  ", 10, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new Stand(null, 10, BigDecimal.TEN));
    }

    @Test
    void rechazaMetrosCuadradosEnCeroONegativos() {
        assertThrows(IllegalArgumentException.class, () -> new Stand("A-1", 0, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new Stand("A-1", -3, BigDecimal.TEN));
    }

    @Test
    void rechazaUnPrecioNuloONoPositivo() {
        assertThrows(IllegalArgumentException.class, () -> new Stand("A-1", 10, null));
        assertThrows(IllegalArgumentException.class, () -> new Stand("A-1", 10, BigDecimal.ZERO));
    }

    @Test
    void unaTransicionValidaCambiaElEstado() {
        Stand s = new Stand("A-1", 10, BigDecimal.TEN);
        s.cambiarEstado(EstadoStand.RESERVADO);
        s.cambiarEstado(EstadoStand.OCUPADO);
        assertEquals(EstadoStand.OCUPADO, s.getEstado());
    }

    @Test
    void unaTransicionInvalidaLanzaExcepcionYConservaElEstado() {
        Stand s = new Stand("A-1", 10, BigDecimal.TEN);
        IllegalStateException e = assertThrows(IllegalStateException.class,
            () -> s.cambiarEstado(EstadoStand.OCUPADO));

        assertEquals(EstadoStand.DISPONIBLE, s.getEstado());
        assertEquals("El stand A-1 no puede pasar de DISPONIBLE a OCUPADO", e.getMessage());
    }
}
