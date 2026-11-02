package mx.filey.stands.datos;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Abono hecho por una editorial sobre su reservación. Es inmutable.
 */
public final class Pago {
    private final BigDecimal monto;
    private final LocalDate fecha;

    public Pago(BigDecimal monto, LocalDate fecha) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El pago debe ser mayor a cero");
        }
        if (fecha == null) {
            throw new IllegalArgumentException("El pago necesita una fecha");
        }
        this.monto = monto;
        this.fecha = fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    @Override
    public String toString() {
        return "$" + monto + " (" + fecha + ")";
    }
}
