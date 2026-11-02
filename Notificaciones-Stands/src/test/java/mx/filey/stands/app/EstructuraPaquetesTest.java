package mx.filey.stands.app;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import mx.filey.stands.configuracion.Configuracion;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;
import mx.filey.stands.negocio.ServicioReservaciones;
import mx.filey.stands.negocio.ServicioStands;
import mx.filey.stands.notificaciones.Notificador;
import mx.filey.stands.notificaciones.ServicioNotificaciones;

/** RNF-3: las clases viven en los paquetes definidos para el proyecto. */
class EstructuraPaquetesTest {

    @Test
    void lasClasesDeNegocioEstanEnElPaqueteNegocio() {
        assertEquals("mx.filey.stands.negocio", ServicioReservaciones.class.getPackageName());
        assertEquals("mx.filey.stands.negocio", ServicioStands.class.getPackageName());
    }

    @Test
    void lasClasesDeNotificacionesEstanEnSuPaquete() {
        assertEquals("mx.filey.stands.notificaciones", ServicioNotificaciones.class.getPackageName());
        assertEquals("mx.filey.stands.notificaciones", Notificador.class.getPackageName());
    }

    @Test
    void laConfiguracionTieneSuPaquete() {
        assertEquals("mx.filey.stands.configuracion", Configuracion.class.getPackageName());
    }

    @Test
    void lasEntidadesEstanEnElPaqueteDatos() {
        assertEquals("mx.filey.stands.datos", Stand.class.getPackageName());
        assertEquals("mx.filey.stands.datos", Reservacion.class.getPackageName());
    }

    @Test
    void elPuntoDeEntradaEstaEnElPaqueteApp() {
        assertEquals("mx.filey.stands.app", Main.class.getPackageName());
    }
}
