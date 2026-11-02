package mx.filey.stands.configuracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ConfiguracionTest {
    private static final LocalDate PRONTO = LocalDate.of(2027, 1, 31);
    private static final LocalDate MAXIMA = LocalDate.of(2027, 2, 15);

    private static final String ARCHIVO_VALIDO =
        "precioPorMetroCuadrado=2000.00\n"
        + "diasVigenciaReserva=7\n"
        + "porcentajeMinimoOcupacion=25\n"
        + "porcentajeDescuentoProntoPago=15\n"
        + "fechaLimiteProntoPago=2027-03-01\n"
        + "fechaMaximaPago=2027-04-01\n"
        + "diasAvisoVencimiento=3\n";

    private static Path escribir(String contenido) throws IOException {
        Path archivo = Files.createTempFile("configuracion-prueba", ".properties");
        Files.writeString(archivo, contenido);
        archivo.toFile().deleteOnExit();
        return archivo;
    }

    @Test
    void cargaLosParametrosDelArchivoIncluidoEnElPrograma() {
        Configuracion c = Configuracion.cargarPorDefecto();

        assertEquals(new BigDecimal("1500.00"), c.getPrecioPorMetroCuadrado());
        assertEquals(15, c.getDiasVigenciaReserva());
        assertEquals(30.0, c.getPorcentajeMinimoOcupacion());
        assertEquals(10.0, c.getPorcentajeDescuentoProntoPago());
        assertEquals(PRONTO, c.getFechaLimiteProntoPago());
        assertEquals(MAXIMA, c.getFechaMaximaPago());
        assertEquals(5, c.getDiasAvisoVencimiento());
    }

    @Test
    void cargaLosParametrosDeUnArchivoExterno() throws IOException {
        Configuracion c = Configuracion.cargarDesdeArchivo(escribir(ARCHIVO_VALIDO));

        assertEquals(new BigDecimal("2000.00"), c.getPrecioPorMetroCuadrado());
        assertEquals(7, c.getDiasVigenciaReserva());
        assertEquals(25.0, c.getPorcentajeMinimoOcupacion());
        assertEquals(15.0, c.getPorcentajeDescuentoProntoPago());
        assertEquals(LocalDate.of(2027, 3, 1), c.getFechaLimiteProntoPago());
        assertEquals(LocalDate.of(2027, 4, 1), c.getFechaMaximaPago());
        assertEquals(3, c.getDiasAvisoVencimiento());
    }

    @Test
    void aceptaUnaFechaDeProntoPagoIgualALaFechaMaxima() {
        Configuracion c = new Configuracion(new BigDecimal("1000"), 10, 30, 10, MAXIMA, MAXIMA, 5);
        assertEquals(MAXIMA, c.getFechaLimiteProntoPago());
    }

    @Test
    void rechazaUnaFechaDeProntoPagoPosteriorALaFechaMaxima() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> new Configuracion(
            new BigDecimal("1000"), 10, 30, 10, LocalDate.of(2027, 3, 1), MAXIMA, 5));
        assertTrue(e.getMessage().contains("pronto pago"));
    }

    @Test
    void rechazaUnPrecioPorMetroNoPositivo() {
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(BigDecimal.ZERO, 10, 30, 10, PRONTO, MAXIMA, 5));
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(new BigDecimal("-5"), 10, 30, 10, PRONTO, MAXIMA, 5));
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(null, 10, 30, 10, PRONTO, MAXIMA, 5));
    }

    @Test
    void rechazaUnaVigenciaMenorAUnDia() {
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(new BigDecimal("1000"), 0, 30, 10, PRONTO, MAXIMA, 5));
    }

    @Test
    void rechazaUnPorcentajeMinimoFueraDeRango() {
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(new BigDecimal("1000"), 10, 0, 10, PRONTO, MAXIMA, 5));
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(new BigDecimal("1000"), 10, 101, 10, PRONTO, MAXIMA, 5));
    }

    @Test
    void rechazaUnDescuentoFueraDeRango() {
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(new BigDecimal("1000"), 10, 30, -1, PRONTO, MAXIMA, 5));
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(new BigDecimal("1000"), 10, 30, 100, PRONTO, MAXIMA, 5));
    }

    @Test
    void rechazaDiasDeAvisoNegativosPeroAceptaCero() {
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(new BigDecimal("1000"), 10, 30, 10, PRONTO, MAXIMA, -1));
        assertEquals(0,
            new Configuracion(new BigDecimal("1000"), 10, 30, 10, PRONTO, MAXIMA, 0).getDiasAvisoVencimiento());
    }

    @Test
    void rechazaFechasNulas() {
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(new BigDecimal("1000"), 10, 30, 10, null, MAXIMA, 5));
        assertThrows(IllegalArgumentException.class,
            () -> new Configuracion(new BigDecimal("1000"), 10, 30, 10, PRONTO, null, 5));
    }

    @Test
    void unArchivoSinUnParametroIndicaCualFalta() throws IOException {
        String sinVigencia = ARCHIVO_VALIDO.replace("diasVigenciaReserva=7\n", "");
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
            () -> Configuracion.cargarDesdeArchivo(escribir(sinVigencia)));
        assertTrue(e.getMessage().contains("diasVigenciaReserva"));
    }

    @Test
    void unArchivoConUnValorMalFormadoLoRechaza() throws IOException {
        String fechaMala = ARCHIVO_VALIDO.replace("2027-03-01", "01/03/2027");
        String numeroMalo = ARCHIVO_VALIDO.replace("diasVigenciaReserva=7", "diasVigenciaReserva=siete");

        assertThrows(IllegalArgumentException.class,
            () -> Configuracion.cargarDesdeArchivo(escribir(fechaMala)));
        assertThrows(IllegalArgumentException.class,
            () -> Configuracion.cargarDesdeArchivo(escribir(numeroMalo)));
    }

    @Test
    void unArchivoInexistenteLanzaUnErrorDeLectura() {
        Path noExiste = Path.of("no-existe-" + System.nanoTime() + ".properties");
        assertThrows(UncheckedIOException.class, () -> Configuracion.cargarDesdeArchivo(noExiste));
    }

    @Test
    void toStringMuestraTodosLosParametros() {
        String texto = Configuracion.cargarPorDefecto().toString();
        assertTrue(texto.contains("1500.00"));
        assertTrue(texto.contains("2027-01-31"));
        assertTrue(texto.contains("2027-02-15"));
    }
}
