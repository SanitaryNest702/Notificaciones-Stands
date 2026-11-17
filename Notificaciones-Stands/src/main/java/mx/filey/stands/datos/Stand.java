package mx.filey.stands.datos;

import java.math.BigDecimal;

/**
 * Espacio de la feria que puede venderse a una editorial.
 */
public class Stand {
    private final String id;
    private final double metrosCuadrados;
    private final BigDecimal precio;
    private EstadoStand estado;

    public Stand(String id, double metrosCuadrados, BigDecimal precio) {
        this(id, metrosCuadrados, precio, EstadoStand.DISPONIBLE);
    }

    /**
     * Reconstruye un stand con el estado que ya tenía (por ejemplo, al leerlo de la base de datos).
     * No valida transiciones: el estado se toma tal cual.
     */
    public Stand(String id, double metrosCuadrados, BigDecimal precio, EstadoStand estadoInicial) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El stand necesita un identificador");
        }
        if (metrosCuadrados <= 0) {
            throw new IllegalArgumentException("Los metros cuadrados deben ser mayores a cero");
        }
        if (precio == null || precio.signum() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        this.id = id;
        this.metrosCuadrados = metrosCuadrados;
        this.precio = precio;
        if (estadoInicial == null) {
            throw new IllegalArgumentException("El estado inicial no puede ser nulo");
        }
        this.estado = estadoInicial;
    }

    public String getId() {
        return id;
    }

    public double getMetrosCuadrados() {
        return metrosCuadrados;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public EstadoStand getEstado() {
        return estado;
    }

    /**
     * Cambia el estado validando que la transición esté permitida.
     *
     * @throws IllegalStateException si el cambio no es válido (por ejemplo OCUPADO a DISPONIBLE)
     */
    public void cambiarEstado(EstadoStand nuevo) {
        if (!estado.puedePasarA(nuevo)) {
            throw new IllegalStateException(
                "El stand " + id + " no puede pasar de " + estado + " a " + nuevo);
        }
        this.estado = nuevo;
    }

    @Override
    public String toString() {
        return id + " (" + metrosCuadrados + " m2, $" + precio + ", " + estado + ")";
    }
}
