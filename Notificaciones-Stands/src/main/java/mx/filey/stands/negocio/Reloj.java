package mx.filey.stands.negocio;

import java.time.LocalDate;

/**
 * Fuente de la fecha actual. Los servicios dependen de esta interfaz y no de
 * la fecha real, así se pueden simular vencimientos sin esperar días de verdad.
 */
public interface Reloj {

    LocalDate hoy();
}
