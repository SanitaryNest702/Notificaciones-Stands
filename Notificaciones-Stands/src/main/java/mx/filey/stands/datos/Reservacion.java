package mx.filey.stands.datos;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Relación entre una editorial y un stand: guarda la vigencia de la reserva
 * y el historial de pagos.
 */
public class Reservacion {
    private final String id;
    private final Stand stand;
    private final Editorial editorial;
    private final LocalDate fechaReserva;
    private final LocalDate fechaVigencia;
    private final List<Pago> pagos = new ArrayList<>();

    private BigDecimal costoAplicable;
    private boolean descuentoAsegurado = false;
    private boolean activa = true;

    public Reservacion(String id, Stand stand, Editorial editorial,
                       LocalDate fechaReserva, LocalDate fechaVigencia) {
        if (stand == null || editorial == null) {
            throw new IllegalArgumentException("La reservacion necesita un stand y una editorial");
        }
        if (fechaVigencia.isBefore(fechaReserva)) {
            throw new IllegalArgumentException("La vigencia no puede ser anterior a la reserva");
        }
        this.id = id;
        this.stand = stand;
        this.editorial = editorial;
        this.fechaReserva = fechaReserva;
        this.fechaVigencia = fechaVigencia;
        this.costoAplicable = stand.getPrecio();
    }

    public String getId() {
        return id;
    }

    public Stand getStand() {
        return stand;
    }

    public Editorial getEditorial() {
        return editorial;
    }

    public LocalDate getFechaReserva() {
        return fechaReserva;
    }

    public LocalDate getFechaVigencia() {
        return fechaVigencia;
    }

    public BigDecimal getCostoAplicable() {
        return costoAplicable;
    }

    /** Historial de pagos, en el orden en que se registraron (solo lectura). */
    public List<Pago> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    public BigDecimal getMontoPagado() {
        BigDecimal total = BigDecimal.ZERO;
        for (Pago p : pagos) {
            total = total.add(p.getMonto());
        }
        return total;
    }

    public boolean isDescuentoAsegurado() {
        return descuentoAsegurado;
    }

    public boolean isActiva() {
        return activa;
    }

    /** Fija el costo con descuento; solo se llama en el primer pago. */
    public void asegurarDescuento(BigDecimal costoConDescuento) {
        this.costoAplicable = costoConDescuento;
        this.descuentoAsegurado = true;
    }

    public void registrarPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago no puede ser nulo");
        }
        pagos.add(pago);
    }

    /** Porcentaje del costo ya cubierto, de 0 a 100. */
    public double getPorcentajePagado() {
        return getMontoPagado()
            .multiply(BigDecimal.valueOf(100))
            .divide(costoAplicable, 2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    /**
     * Indica si lo pagado cubre al menos el porcentaje dado, comparando montos exactos
     * (sin el redondeo a 2 decimales de {@link #getPorcentajePagado()}).
     */
    public boolean haCubiertoAlMenos(double porcentaje) {
        BigDecimal pagadoPor100 = getMontoPagado().multiply(BigDecimal.valueOf(100));
        BigDecimal requeridoPor100 = costoAplicable.multiply(BigDecimal.valueOf(porcentaje));
        return pagadoPor100.compareTo(requeridoPor100) >= 0;
    }

    public BigDecimal getSaldoPendiente() {
        return costoAplicable.subtract(getMontoPagado()).max(BigDecimal.ZERO);
    }

    public boolean estaPagadaCompleta() {
        return getMontoPagado().compareTo(costoAplicable) >= 0;
    }

    public void cancelar() {
        this.activa = false;
    }

    @Override
    public String toString() {
        return id + ": " + stand.getId() + " -> " + editorial.getNombre()
            + " (pagado " + getPorcentajePagado() + "%)";
    }
}
