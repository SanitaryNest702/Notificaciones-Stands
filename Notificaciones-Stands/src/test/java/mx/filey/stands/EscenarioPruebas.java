package mx.filey.stands;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import mx.filey.stands.configuracion.Configuracion;
import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Repositorio;
import mx.filey.stands.datos.RepositorioEnMemoria;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;
import mx.filey.stands.negocio.CalculadoraPrecios;
import mx.filey.stands.negocio.RelojSimulado;
import mx.filey.stands.negocio.ServicioReservaciones;
import mx.filey.stands.negocio.ServicioStands;
import mx.filey.stands.negocio.ServicioTransferencias;
import mx.filey.stands.notificaciones.Notificacion;
import mx.filey.stands.notificaciones.ServicioNotificaciones;

/**
 * Arma un sistema completo con fechas fijas para que las pruebas sean repetibles.
 * Precio: $1000 por m2. Reserva: 10 dias. Minimo para ocupar: 30%.
 * Descuento: 10% hasta el 2027-01-31. Fecha maxima de pago: 2027-02-15. Aviso: 5 dias.
 * Stands S-1 y S-2 de 10 m2 ($10,000). Editoriales E-1 y E-2.
 */
public class EscenarioPruebas {
    public final Configuracion configuracion = new Configuracion(
        new BigDecimal("1000.00"), 10, 30, 10,
        LocalDate.of(2027, 1, 31), LocalDate.of(2027, 2, 15), 5);

    public final RelojSimulado reloj;
    public final Repositorio<Stand> stands = new RepositorioEnMemoria<>(Stand::getId);
    public final Repositorio<Editorial> editoriales = new RepositorioEnMemoria<>(Editorial::getId);
    public final Repositorio<Reservacion> reservaciones = new RepositorioEnMemoria<>(Reservacion::getId);
    public final CalculadoraPrecios calculadora = new CalculadoraPrecios(configuracion);
    public final ServicioStands servicioStands;
    public final ServicioReservaciones servicioReservaciones;
    public final ServicioTransferencias servicioTransferencias;
    public final ServicioNotificaciones servicioNotificaciones;
    public final List<Notificacion> avisosEntregados = new ArrayList<>();

    public EscenarioPruebas(LocalDate fechaInicial) {
        this.reloj = new RelojSimulado(fechaInicial);

        servicioStands = new ServicioStands(stands, calculadora);
        servicioReservaciones = new ServicioReservaciones(
            servicioStands, editoriales, reservaciones, calculadora, configuracion, reloj);
        servicioTransferencias = new ServicioTransferencias(servicioStands, servicioReservaciones);
        servicioNotificaciones = new ServicioNotificaciones(
            reservaciones, avisosEntregados::add, configuracion, reloj);

        editoriales.guardar(new Editorial("E-1", "Editorial Uno", "uno@example.com"));
        editoriales.guardar(new Editorial("E-2", "Editorial Dos", "dos@example.com"));
        servicioStands.crearStand("S-1", 10);   // $10000
        servicioStands.crearStand("S-2", 10);
    }
}
