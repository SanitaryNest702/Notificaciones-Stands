package mx.filey.stands.negocio;

import mx.filey.stands.datos.EstadoStand;
import mx.filey.stands.datos.Reservacion;
import mx.filey.stands.datos.Stand;

/**
 * Transferencia de un stand bloqueado a una editorial: queda reservado a su nombre,
 * para que pueda completar la compra.
 */
public class ServicioTransferencias {
    private final ServicioStands servicioStands;
    private final ServicioReservaciones servicioReservaciones;

    public ServicioTransferencias(ServicioStands servicioStands,
                                  ServicioReservaciones servicioReservaciones) {
        this.servicioStands = servicioStands;
        this.servicioReservaciones = servicioReservaciones;
    }

    /** Pasa un stand de Bloqueado a Reservado a nombre de la editorial indicada. */
    public Reservacion transferir(String idStand, String idEditorial) {
        Stand stand = servicioStands.obtener(idStand);
        if (stand.getEstado() != EstadoStand.BLOQUEADO) {
            throw new NegocioException("Solo se pueden transferir stands bloqueados; el stand "
                + idStand + " esta " + stand.getEstado());
        }
        return servicioReservaciones.crearReservacion(idStand, idEditorial);
    }
}
