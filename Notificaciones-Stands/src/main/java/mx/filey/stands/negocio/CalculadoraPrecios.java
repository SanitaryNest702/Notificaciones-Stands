package mx.filey.stands.negocio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import mx.filey.stands.configuracion.Configuracion;

/**
 * Reglas de costo: precio por dimensiones y descuento por pronto pago.
 */
public class CalculadoraPrecios {
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final Configuracion configuracion;

    public CalculadoraPrecios(Configuracion configuracion) {
        this.configuracion = configuracion;
    }

    /** Precio de un stand según sus metros cuadrados. */
    public BigDecimal precioDeStand(double metrosCuadrados) {
        return BigDecimal.valueOf(metrosCuadrados)
            .multiply(configuracion.getPrecioPorMetroCuadrado())
            .setScale(2, RoundingMode.HALF_UP);
    }

    /** Indica si pagar en esa fecha todavía da derecho al descuento. */
    public boolean tieneDescuento(LocalDate fechaPago) {
        return !fechaPago.isAfter(configuracion.getFechaLimiteProntoPago());
    }

    /**
     * Costo que debe cubrir la editorial: con descuento si el pago es a tiempo,
     * y sin descuento en caso contrario.
     */
    public BigDecimal costoAplicable(BigDecimal precio, LocalDate fechaPago) {
        if (!tieneDescuento(fechaPago)) {
            return precio;
        }
        BigDecimal factor = CIEN.subtract(BigDecimal.valueOf(configuracion.getPorcentajeDescuentoProntoPago()))
            .divide(CIEN, 4, RoundingMode.HALF_UP);
        return precio.multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
