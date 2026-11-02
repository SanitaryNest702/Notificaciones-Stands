package mx.filey.stands.negocio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import mx.filey.stands.configuracion.Configuracion;
import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Pago;
import mx.filey.stands.datos.Repositorio;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;

/**
 * Flujo de venta: reservar un stand, registrar pagos, pasar a Ocupado
 * y liberar las reservas que vencen.
 */
public class ServicioReservaciones {
    private final ServicioStands servicioStands;
    private final Repositorio<Editorial> editoriales;
    private final Repositorio<Reservacion> reservaciones;
    private final CalculadoraPrecios calculadora;
    private final Configuracion configuracion;
    private final Reloj reloj;

    public ServicioReservaciones(ServicioStands servicioStands,
                                 Repositorio<Editorial> editoriales,
                                 Repositorio<Reservacion> reservaciones,
                                 CalculadoraPrecios calculadora,
                                 Configuracion configuracion,
                                 Reloj reloj) {
        this.servicioStands = servicioStands;
        this.editoriales = editoriales;
        this.reservaciones = reservaciones;
        this.calculadora = calculadora;
        this.configuracion = configuracion;
        this.reloj = reloj;
    }

    /** Una editorial aparta un stand disponible para pagar después. */
    public Reservacion reservar(String idStand, String idEditorial) {
        Stand stand = servicioStands.obtener(idStand);
        if (stand.getEstado() != EstadoStand.DISPONIBLE) {
            throw new NegocioException(
                "El stand " + idStand + " no esta disponible (estado: " + stand.getEstado() + ")");
        }
        return crearReservacion(idStand, idEditorial);
    }

    /**
     * Pasa el stand a Reservado (desde Disponible o Bloqueado) y crea la reservación.
     * Es de paquete porque también la usa {@link ServicioTransferencias}.
     */
    Reservacion crearReservacion(String idStand, String idEditorial) {
        Editorial editorial = editoriales.buscarPorId(idEditorial)
            .orElseThrow(() -> new NegocioException("No existe la editorial " + idEditorial));
        Stand stand = servicioStands.obtener(idStand);

        servicioStands.cambiarEstado(idStand, EstadoStand.RESERVADO);

        LocalDate hoy = reloj.hoy();
        String id = "R-" + (reservaciones.listar().size() + 1);
        Reservacion reservacion = new Reservacion(
            id, stand, editorial, hoy, hoy.plusDays(configuracion.getDiasVigenciaReserva()));
        reservaciones.guardar(reservacion);
        return reservacion;
    }

    /**
     * Registra un pago. El primer pago define si hay descuento por pronto pago.
     * Si los pagos cubren el porcentaje mínimo, el stand pasa a Ocupado.
     */
    public Reservacion registrarPago(String idReservacion, BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new NegocioException("El pago debe ser mayor a cero");
        }
        Reservacion reservacion = obtener(idReservacion);
        Stand stand = reservacion.getStand();
        LocalDate hoy = reloj.hoy();

        if (!reservacion.isActiva()) {
            throw new NegocioException("La reservacion " + idReservacion + " ya no esta activa");
        }
        if (stand.getEstado() == EstadoStand.RESERVADO && hoy.isAfter(reservacion.getFechaVigencia())) {
            throw new NegocioException("La reserva vencio el " + reservacion.getFechaVigencia());
        }
        // El primer pago define el costo: con descuento si llega a tiempo.
        boolean aplicaDescuento = reservacion.getMontoPagado().signum() == 0
            && calculadora.tieneDescuento(hoy);
        BigDecimal costo = aplicaDescuento
            ? calculadora.costoAplicable(stand.getPrecio(), hoy)
            : reservacion.getCostoAplicable();
        BigDecimal saldo = costo.subtract(reservacion.getMontoPagado());

        if (monto.compareTo(saldo) > 0) {
            throw new NegocioException("El pago excede el saldo pendiente ($" + saldo + ")");
        }

        if (aplicaDescuento) {
            reservacion.asegurarDescuento(costo);
        }
        reservacion.registrarPago(new Pago(monto, hoy));

        if (stand.getEstado() == EstadoStand.RESERVADO
                && reservacion.haCubiertoAlMenos(configuracion.getPorcentajeMinimoOcupacion())) {
            servicioStands.cambiarEstado(stand.getId(), EstadoStand.OCUPADO);
        }
        return reservacion;
    }

    /**
     * Cancela las reservas vencidas que no alcanzaron el porcentaje mínimo
     * y devuelve sus stands a Disponible.
     */
    public List<Reservacion> liberarReservasVencidas() {
        LocalDate hoy = reloj.hoy();
        List<Reservacion> liberadas = new ArrayList<>();
        for (Reservacion r : reservaciones.listar()) {
            boolean vencida = r.isActiva()
                && r.getStand().getEstado() == EstadoStand.RESERVADO
                && hoy.isAfter(r.getFechaVigencia());
            if (vencida) {
                r.cancelar();
                servicioStands.cambiarEstado(r.getStand().getId(), EstadoStand.DISPONIBLE);
                liberadas.add(r);
            }
        }
        return liberadas;
    }

    public Reservacion obtener(String id) {
        return reservaciones.buscarPorId(id)
            .orElseThrow(() -> new NegocioException("No existe la reservacion " + id));
    }

    public List<Reservacion> listarActivas() {
        List<Reservacion> activas = new ArrayList<>();
        for (Reservacion r : reservaciones.listar()) {
            if (r.isActiva()) {
                activas.add(r);
            }
        }
        return activas;
    }
}
