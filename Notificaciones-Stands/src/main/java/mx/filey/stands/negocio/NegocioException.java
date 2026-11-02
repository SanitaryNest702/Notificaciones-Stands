package mx.filey.stands.negocio;

/**
 * Se lanza cuando una operación viola una regla de negocio
 * (por ejemplo reservar un stand que ya está ocupado).
 */
public class NegocioException extends RuntimeException {

    public NegocioException(String mensaje) {
        super(mensaje);
    }
}
