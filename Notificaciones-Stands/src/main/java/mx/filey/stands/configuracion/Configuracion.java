package mx.filey.stands.configuracion;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Properties;

/**
 * Parámetros de costos y plazos del sistema. Es inmutable: una vez creada no cambia.
 */
public class Configuracion {
    private final BigDecimal precioPorMetroCuadrado;
    private final int diasVigenciaReserva;
    private final double porcentajeMinimoOcupacion;
    private final double porcentajeDescuentoProntoPago;
    private final LocalDate fechaLimiteProntoPago;
    private final LocalDate fechaMaximaPago;
    private final int diasAvisoVencimiento;

    public Configuracion(BigDecimal precioPorMetroCuadrado,
                         int diasVigenciaReserva,
                         double porcentajeMinimoOcupacion,
                         double porcentajeDescuentoProntoPago,
                         LocalDate fechaLimiteProntoPago,
                         LocalDate fechaMaximaPago,
                         int diasAvisoVencimiento) {
        if (precioPorMetroCuadrado == null || precioPorMetroCuadrado.signum() <= 0) {
            throw new IllegalArgumentException("El precio por metro cuadrado debe ser mayor a cero");
        }
        if (diasVigenciaReserva <= 0) {
            throw new IllegalArgumentException("La vigencia de la reserva debe ser de al menos 1 dia");
        }
        if (porcentajeMinimoOcupacion <= 0 || porcentajeMinimoOcupacion > 100) {
            throw new IllegalArgumentException("El porcentaje minimo debe estar entre 0 (sin incluir) y 100");
        }
        if (porcentajeDescuentoProntoPago < 0 || porcentajeDescuentoProntoPago >= 100) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100 (sin incluir)");
        }
        if (diasAvisoVencimiento < 0) {
            throw new IllegalArgumentException("Los dias de aviso no pueden ser negativos");
        }
        if (fechaLimiteProntoPago == null || fechaMaximaPago == null) {
            throw new IllegalArgumentException("Faltan las fechas de pronto pago y de pago maximo");
        }
        if (fechaLimiteProntoPago.isAfter(fechaMaximaPago)) {
            throw new IllegalArgumentException(
                "La fecha de pronto pago no puede ser posterior a la fecha maxima de pago");
        }
        this.precioPorMetroCuadrado = precioPorMetroCuadrado;
        this.diasVigenciaReserva = diasVigenciaReserva;
        this.porcentajeMinimoOcupacion = porcentajeMinimoOcupacion;
        this.porcentajeDescuentoProntoPago = porcentajeDescuentoProntoPago;
        this.fechaLimiteProntoPago = fechaLimiteProntoPago;
        this.fechaMaximaPago = fechaMaximaPago;
        this.diasAvisoVencimiento = diasAvisoVencimiento;
    }

    public BigDecimal getPrecioPorMetroCuadrado() {
        return precioPorMetroCuadrado;
    }

    public int getDiasVigenciaReserva() {
        return diasVigenciaReserva;
    }

    public double getPorcentajeMinimoOcupacion() {
        return porcentajeMinimoOcupacion;
    }

    public double getPorcentajeDescuentoProntoPago() {
        return porcentajeDescuentoProntoPago;
    }

    public LocalDate getFechaLimiteProntoPago() {
        return fechaLimiteProntoPago;
    }

    public LocalDate getFechaMaximaPago() {
        return fechaMaximaPago;
    }

    public int getDiasAvisoVencimiento() {
        return diasAvisoVencimiento;
    }

    /** Carga la configuración desde un archivo {@code .properties} del disco. */
    public static Configuracion cargarDesdeArchivo(Path ruta) {
        try (InputStream entrada = Files.newInputStream(ruta)) {
            return desdeFlujo(entrada);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la configuracion: " + ruta, e);
        }
    }

    /** Carga la configuración incluida dentro del programa (carpeta resources). */
    public static Configuracion cargarPorDefecto() {
        try (InputStream entrada =
                 Configuracion.class.getResourceAsStream("/configuracion.properties")) {
            if (entrada == null) {
                throw new IllegalStateException("Falta el archivo configuracion.properties");
            }
            return desdeFlujo(entrada);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la configuracion", e);
        }
    }

    private static Configuracion desdeFlujo(InputStream entrada) throws IOException {
        Properties p = new Properties();
        p.load(entrada);
        try {
            return new Configuracion(
                new BigDecimal(requerida(p, "precioPorMetroCuadrado")),
                Integer.parseInt(requerida(p, "diasVigenciaReserva")),
                Double.parseDouble(requerida(p, "porcentajeMinimoOcupacion")),
                Double.parseDouble(requerida(p, "porcentajeDescuentoProntoPago")),
                LocalDate.parse(requerida(p, "fechaLimiteProntoPago")),
                LocalDate.parse(requerida(p, "fechaMaximaPago")),
                Integer.parseInt(requerida(p, "diasAvisoVencimiento")));
        } catch (NumberFormatException | DateTimeParseException e) {
            throw new IllegalArgumentException("La configuracion tiene un valor con formato invalido: "
                + e.getMessage(), e);
        }
    }

    private static String requerida(Properties p, String clave) {
        String valor = p.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el parametro de configuracion: " + clave);
        }
        return valor.trim();
    }

    @Override
    public String toString() {
        return "Precio por m2:            $" + precioPorMetroCuadrado + "\n"
            + "Vigencia de reserva:      " + diasVigenciaReserva + " dias\n"
            + "% minimo para ocupar:     " + porcentajeMinimoOcupacion + "%\n"
            + "Descuento pronto pago:    " + porcentajeDescuentoProntoPago + "%\n"
            + "Fecha limite pronto pago: " + fechaLimiteProntoPago + "\n"
            + "Fecha maxima de pago:     " + fechaMaximaPago + "\n"
            + "Dias de aviso:            " + diasAvisoVencimiento;
    }
}
