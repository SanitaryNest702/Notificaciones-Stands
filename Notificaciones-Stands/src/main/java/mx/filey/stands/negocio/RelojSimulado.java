package mx.filey.stands.negocio;

import java.time.LocalDate;

/**
 * Reloj cuya fecha se puede adelantar. Se usa en la línea de comandos
 * (opción de simulación) y en las pruebas.
 */
public class RelojSimulado implements Reloj {
    private LocalDate fechaActual;

    /** Reloj que arranca en el día de hoy. */
    public RelojSimulado() {
        this(LocalDate.now());
    }

    /** Reloj que arranca en una fecha concreta (útil para pruebas). */
    public RelojSimulado(LocalDate fechaInicial) {
        this.fechaActual = fechaInicial;
    }

    @Override
    public LocalDate hoy() {
        return fechaActual;
    }

    public void avanzarDias(int dias) {
        if (dias < 0) {
            throw new IllegalArgumentException("No se puede retroceder el reloj");
        }
        this.fechaActual = fechaActual.plusDays(dias);
    }
}
