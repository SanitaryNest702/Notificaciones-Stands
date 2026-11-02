package mx.filey.stands.app;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import mx.filey.stands.datos.Editorial;
import mx.filey.stands.datos.Repositorio;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.negocio.RelojSimulado;
import mx.filey.stands.negocio.ServicioReservaciones;
import mx.filey.stands.negocio.ServicioStands;

/**
 * Datos de ejemplo para probar el sistema desde la línea de comandos.
 * La consola no crea entidades: todo lo que se ve nace aquí al iniciar.
 */
public final class DatosDemo {

    /** Dias de historia que simula la precarga: la primera reserva se hizo hace tantos dias. */
    public static final int DIAS_DE_HISTORIAL = 14;

    private DatosDemo() {
    }

    /** Crea 8 stands de distintos tamaños (A-01 a A-04 chicos, B-01 a B-04 grandes). */
    public static void cargarStands(ServicioStands servicioStands) {
        servicioStands.crearStand("A-01", 9);
        servicioStands.crearStand("A-02", 9);
        servicioStands.crearStand("A-03", 12);
        servicioStands.crearStand("A-04", 12);
        servicioStands.crearStand("B-01", 18);
        servicioStands.crearStand("B-02", 18);
        servicioStands.crearStand("B-03", 24);
        servicioStands.crearStand("B-04", 36);
    }

    public static void cargarEditoriales(Repositorio<Editorial> editoriales) {
        editoriales.guardar(new Editorial("E-1", "Editorial Horizonte", "contacto@horizonte.example"));
        editoriales.guardar(new Editorial("E-2", "Ediciones del Sur", "ventas@delsur.example"));
        editoriales.guardar(new Editorial("E-3", "Libros Peninsulares", "info@peninsulares.example"));
        editoriales.guardar(new Editorial("E-4", "Editoriales del Bombo", "info@bombo.example"));
    }

    /** Reloj que arranca {@link #DIAS_DE_HISTORIAL} dias atras; la precarga lo deja en el dia de hoy. */
    public static RelojSimulado crearReloj() {
        return new RelojSimulado(LocalDate.now().minusDays(DIAS_DE_HISTORIAL));
    }

    /**
     * Crea reservaciones en distintos momentos del pasado reciente (avanzando el reloj entre
     * una y otra) para que cada una tenga su propia vigencia. Al terminar el reloj marca hoy.
     * Con 15 dias de vigencia y 5 de aviso (configuracion por defecto), "hoy" queda asi:
     * <ul>
     *   <li>R-1 A-01 Horizonte: reservada hace 14 dias, sin pago; vence manana.</li>
     *   <li>R-2 A-02 Del Sur: reservada hace 12 dias con un pago parcial (bajo el minimo para ocupar); vence en 3 dias.</li>
     *   <li>R-3 B-01 Peninsulares: reservada hace 9 dias con pago de mas del minimo; stand Ocupado.</li>
     *   <li>R-4 A-03 Horizonte: reservada hace 3 dias, sin pago; vence en 12 dias.</li>
     *   <li>R-5 B-02 Del Sur: recien creada hoy; vence en 15 dias.</li>
     *   <li>B-04 queda Bloqueado por el administrador; A-04 y B-03 siguen Disponibles.</li>
     * </ul>
     * Debe llamarse con un reloj creado por {@link #crearReloj()}.
     */
    public static List<Reservacion> cargarReservaciones(ServicioReservaciones servicio,
                                                        ServicioStands servicioStands,
                                                        RelojSimulado reloj) {
        List<Reservacion> creadas = new ArrayList<>();

        creadas.add(servicio.reservar("A-01", "E-1"));                       // hace 14 dias
        reloj.avanzarDias(2);
        servicio.registrarPago("R-1", new BigDecimal("12000"));
        Reservacion parcial = servicio.reservar("A-02", "E-2");              // hace 12 dias
        servicio.registrarPago(parcial.getId(), new BigDecimal("1000"));
        creadas.add(parcial);
        reloj.avanzarDias(3);
        Reservacion ocupada = servicio.reservar("B-01", "E-3");              // hace 9 dias
        servicio.registrarPago(ocupada.getId(), new BigDecimal("10000"));
        creadas.add(ocupada);
        reloj.avanzarDias(6);
        creadas.add(servicio.reservar("A-03", "E-1"));                       // hace 3 dias
        reloj.avanzarDias(3);
        creadas.add(servicio.reservar("B-02", "E-2"));                       // hoy
        creadas.add(servicio.reservar("B-04", "E-4"));
        servicio.registrarPago("R-6", new BigDecimal("4869.5"));
        return creadas;
    }
}
