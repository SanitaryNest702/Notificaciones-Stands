package mx.filey.stands.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Scanner;

import mx.filey.stands.configuracion.Configuracion;
import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Repositorio;
import mx.filey.stands.datos.RepositorioEnMemoria;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;
import mx.filey.stands.negocio.CalculadoraPrecios;
import mx.filey.stands.negocio.NegocioException;
import mx.filey.stands.negocio.RelojSimulado;
import mx.filey.stands.negocio.ServicioReservaciones;
import mx.filey.stands.negocio.ServicioStands;
import mx.filey.stands.notificaciones.Notificacion;
import mx.filey.stands.notificaciones.NotificadorConsola;
import mx.filey.stands.notificaciones.ServicioNotificaciones;

/**
 * Consola de simulacion y auditoria. No crea ni edita entidades: los stands, las editoriales
 * y las reservaciones se precargan desde {@link DatosDemo}; aqui solo se consulta el estado,
 * se avanza la fecha del sistema y se revisan vencimientos.
 * Las reglas viven en el paquete negocio.
 */
public class Main {

    private final Scanner entrada = new Scanner(System.in);
    private final Configuracion configuracion;
    private final RelojSimulado reloj;
    private final CalculadoraPrecios calculadora;
    private final ServicioStands servicioStands;
    private final ServicioReservaciones servicioReservaciones;
    private final ServicioNotificaciones servicioNotificaciones;
    private final NotificadorConsola notificadorConsola = new NotificadorConsola();
    private final List<Reservacion> reservacionesPrecargadas;
    /** Fecha a imprimir antes del siguiente evento al avanzar dias (null = ya impresa o no aplica). */
    private LocalDate encabezadoPendiente;

    public Main() {
        this.configuracion = Configuracion.cargarPorDefecto();
        this.reloj = DatosDemo.crearReloj();

        Repositorio<Stand> stands = new RepositorioEnMemoria<>(Stand::getId);
        Repositorio<Editorial> editoriales = new RepositorioEnMemoria<>(Editorial::getId);
        Repositorio<Reservacion> reservaciones = new RepositorioEnMemoria<>(Reservacion::getId);

        this.calculadora = new CalculadoraPrecios(configuracion);
        this.servicioStands = new ServicioStands(stands, calculadora);
        this.servicioReservaciones = new ServicioReservaciones(
            servicioStands, editoriales, reservaciones, calculadora, configuracion, reloj);
        this.servicioNotificaciones = new ServicioNotificaciones(
            reservaciones, this::mostrarAviso, configuracion, reloj);

        DatosDemo.cargarStands(servicioStands);
        DatosDemo.cargarEditoriales(editoriales);
        this.reservacionesPrecargadas =
            DatosDemo.cargarReservaciones(servicioReservaciones, servicioStands, reloj);
    }

    public static void main(String[] args) {
        new Main().ejecutar();
    }

    /** Bucle principal del menú; es visible en el paquete para poder probarlo. */
    void ejecutar() {
        System.out.println("=== Sistema administrativo de stands FILEY ===");
        mostrarPrecarga();
        boolean salir = false;
        while (!salir) {
            mostrarMenu();
            String opcion = leer("Opcion: ");
            try {
                switch (opcion) {
                    case "1": verEstado(); break;
                    case "2": avanzarFecha(); break;
                    case "3": revisarNotificaciones(); break;
                    case "0": salir = true; break;
                    default: System.out.println("Opcion no valida.");
                }
            } catch (NegocioException e) {
                System.out.println("No se pudo completar: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Dato numerico invalido.");
            } catch (IllegalArgumentException e) {
                System.out.println("Dato invalido: " + e.getMessage());
            }
        }
        System.out.println("Hasta luego.");
    }

    private void mostrarMenu() {
        System.out.println();
        System.out.println("Fecha del sistema: " + reloj.hoy());
        System.out.println(" 1. Ver mapa de stands y reservaciones activas");
        System.out.println(" 2. Avanzar la fecha del sistema (simulacion)");
        System.out.println(" 3. Revisar vencimientos y enviar avisos");
        System.out.println(" 0. Salir");
    }

    /** Resume los datos precargados y, por cada reserva, si en su fecha aplicaba el pronto pago. */
    private void mostrarPrecarga() {
        System.out.println("Datos de demostracion cargados: " + servicioStands.listar().size()
            + " stands y " + reservacionesPrecargadas.size() + " reservaciones.");
        for (Reservacion r : reservacionesPrecargadas) {
            System.out.println(" " + r.getId() + " | stand " + r.getStand().getId() + " | "
                + r.getEditorial().getNombre() + " | apartada el " + r.getFechaReserva()
                + " | " + mensajeProntoPago(r));
        }
        System.out.println("Pronto pago (" + porcentajeDescuento() + "%) hasta "
            + configuracion.getFechaLimiteProntoPago() + "; pago maximo el "
            + configuracion.getFechaMaximaPago() + "; avisos con "
            + configuracion.getDiasAvisoVencimiento() + " dias de anticipacion.");
    }

    /** Dice si, en la fecha en que se aparto la reserva, aplicaba el descuento por pronto pago. */
    private String mensajeProntoPago(Reservacion r) {
        LocalDate fecha = r.getFechaReserva();
        BigDecimal precio = r.getStand().getPrecio();
        if (calculadora.tieneDescuento(fecha)) {
            return "¡Se ha aplicado el " + porcentajeDescuento() + "% de descuento por pronto pago! ($"
                + calculadora.costoAplicable(precio, fecha) + " en lugar de $" + precio + ")";
        }
        return "pronto pago vencido: precio regular $" + precio + " (sin descuento)";
    }

    private String porcentajeDescuento() {
        return BigDecimal.valueOf(configuracion.getPorcentajeDescuentoProntoPago())
            .stripTrailingZeros().toPlainString();
    }

    private void verEstado() {
        verMapa();
        System.out.println();
        verReservaciones();
    }

    private void verMapa() {
        System.out.printf("%-6s %8s %12s  %s%n", "ID", "m2", "Precio", "Estado");
        for (Stand s : servicioStands.listar()) {
            System.out.printf("%-6s %8.1f %12s  %s%n",
                s.getId(), s.getMetrosCuadrados(), "$" + s.getPrecio(), s.getEstado());
        }
    }

    private void verReservaciones() {
        List<Reservacion> activas = servicioReservaciones.listarActivas();
        if (activas.isEmpty()) {
            System.out.println("No hay reservaciones activas.");
            return;
        }
        LocalDate hoy = reloj.hoy();
        for (Reservacion r : activas) {
            long diasRestantes = ChronoUnit.DAYS.between(hoy, r.getFechaVigencia());
            System.out.println("------------------------------------------------------------");
            System.out.println(r.getId() + " | stand " + r.getStand().getId()
                + " | " + r.getEditorial().getNombre());
            System.out.println("  Editorial:      " + r.getEditorial().getNombre()
                + " (" + r.getEditorial().getId() + ")");
            System.out.println("  Stand:          " + r.getStand().getId()
                + " (" + r.getStand().getMetrosCuadrados() + " m2)");
            System.out.println("  Fecha apartado: " + r.getFechaReserva());
            System.out.println("  Vigencia:       hasta " + r.getFechaVigencia()
                + vigenciaTexto(r, diasRestantes));
            System.out.println("  Costo:          $" + r.getCostoAplicable()
                + (r.isDescuentoAsegurado() ? " (con descuento por pronto pago)" : ""));
            System.out.println("  Monto pagado:   $" + r.getMontoPagado()
                + " (" + r.getPorcentajePagado() + "%)  |  saldo $" + r.getSaldoPendiente());
            System.out.println("  Estado:         " + r.getStand().getEstado());
        }
        System.out.println("------------------------------------------------------------");
    }

    private static String vigenciaTexto(Reservacion r, long diasRestantes) {
        if (r.getStand().getEstado() != EstadoStand.RESERVADO) {
            return " (ya no aplica: stand " + r.getStand().getEstado() + ")";
        }
        return diasRestantes >= 0
            ? " (faltan " + diasRestantes + " dias)"
            : " (VENCIDA hace " + (-diasRestantes) + " dias)";
    }

    private void revisarNotificaciones() {
        if (!ejecutarRevision()) {
            System.out.println("No hay avisos nuevos.");
        }
    }

    /**
     * Libera las reservas vencidas y envia los avisos que correspondan a la fecha actual.
     * Cada reservacion se evalua con su propia vigencia.
     *
     * @return true si hubo al menos una liberacion o un aviso
     */
    private boolean ejecutarRevision() {
        List<Reservacion> liberadas = servicioReservaciones.liberarReservasVencidas();
        for (Reservacion r : liberadas) {
            imprimirEncabezado();
            System.out.println("[VENCIDA] " + r.getId() + ": el stand " + r.getStand().getId()
                + " de " + r.getEditorial().getNombre() + " volvio a Disponible.");
        }
        List<Notificacion> enviadas = servicioNotificaciones.revisarVencimientos();
        return !enviadas.isEmpty() || !liberadas.isEmpty();
    }

    private void mostrarAviso(Notificacion notificacion) {
        imprimirEncabezado();
        notificadorConsola.enviar(notificacion);
    }

    private void imprimirEncabezado() {
        if (encabezadoPendiente != null) {
            System.out.println("-- " + encabezadoPendiente + " --");
            encabezadoPendiente = null;
        }
    }

    /**
     * Avanza el reloj dia por dia y revisa cada fecha intermedia, para que ningun aviso
     * ni liberacion se salte al avanzar varios dias de golpe.
     */
    private void avanzarFecha() {
        int dias = Integer.parseInt(leer("Dias a avanzar: "));
        if (dias < 0) {
            reloj.avanzarDias(dias);   // lanza IllegalArgumentException con el mensaje estandar
        }
        for (int i = 0; i < dias; i++) {
            reloj.avanzarDias(1);
            encabezadoPendiente = reloj.hoy();
            ejecutarRevision();
        }
        encabezadoPendiente = null;
        System.out.println("Nueva fecha: " + reloj.hoy());
    }

    private String leer(String mensaje) {
        System.out.print(mensaje);
        return entrada.hasNextLine() ? entrada.nextLine().trim() : "0";
    }
}
