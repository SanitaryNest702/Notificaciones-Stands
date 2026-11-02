package mx.filey.stands.datos;

/**
 * Estados posibles de un stand y las transiciones permitidas entre ellos.
 */
public enum EstadoStand {
    DISPONIBLE,
    RESERVADO,
    OCUPADO,
    BLOQUEADO;

    /**
     * Indica si un stand que está en este estado puede pasar al estado indicado.
     */
    public boolean puedePasarA(EstadoStand nuevo) {
        switch (this) {
            case DISPONIBLE:
                return nuevo == RESERVADO || nuevo == BLOQUEADO;
            case RESERVADO:
                return nuevo == OCUPADO || nuevo == DISPONIBLE;
            case BLOQUEADO:
                return nuevo == DISPONIBLE || nuevo == RESERVADO;
            case OCUPADO:
            default:
                return false;
        }
    }
}
