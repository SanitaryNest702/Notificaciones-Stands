package mx.filey.stands.negocio;

import java.util.List;

import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Repositorio;
import mx.filey.stands.datos.Stand;

/**
 * Administra el mapa de stands y las operaciones del administrador (bloquear y liberar).
 */
public class ServicioStands {
    private final Repositorio<Stand> repositorio;
    private final CalculadoraPrecios calculadora;

    public ServicioStands(Repositorio<Stand> repositorio, CalculadoraPrecios calculadora) {
        this.repositorio = repositorio;
        this.calculadora = calculadora;
    }

    /** Da de alta un stand; su precio se calcula a partir de las dimensiones. */
    public Stand crearStand(String id, double metrosCuadrados) {
        if (id != null && repositorio.buscarPorId(id).isPresent()) {
            throw new NegocioException("Ya existe un stand con el identificador " + id);
        }
        if (id == null || id.isBlank()) {
            throw new NegocioException("El stand necesita un identificador");
        }
        if (metrosCuadrados <= 0) {
            throw new NegocioException("Los metros cuadrados deben ser mayores a cero");
        }
        Stand stand = new Stand(id, metrosCuadrados, calculadora.precioDeStand(metrosCuadrados));
        repositorio.guardar(stand);
        return stand;
    }

    public Stand obtener(String id) {
        return repositorio.buscarPorId(id)
            .orElseThrow(() -> new NegocioException("No existe el stand " + id));
    }

    public List<Stand> listar() {
        return repositorio.listar();
    }

    /** El administrador aparta un stand disponible antes de la salida del sistema. */
    public void bloquear(String id) {
        cambiarEstado(id, EstadoStand.BLOQUEADO);
    }

    /** El administrador regresa un stand bloqueado a Disponible. */
    public void liberarBloqueo(String id) {
        Stand stand = obtener(id);
        if (stand.getEstado() != EstadoStand.BLOQUEADO) {
            throw new NegocioException("El stand " + id + " no esta bloqueado");
        }
        cambiarEstado(id, EstadoStand.DISPONIBLE);
    }

    /** Cambia el estado de un stand traduciendo los errores a {@link NegocioException}. */
    public void cambiarEstado(String id, EstadoStand nuevo) {
        try {
            obtener(id).cambiarEstado(nuevo);
        } catch (IllegalStateException e) {
            throw new NegocioException(e.getMessage());
        }
    }
}
