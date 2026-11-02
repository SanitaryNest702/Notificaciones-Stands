package mx.filey.stands.datos;

import java.util.List;
import java.util.Optional;

/**
 * Contrato para guardar y consultar objetos de tipo {@code T}.
 * Hoy se implementa en memoria; después podría implementarse con una base de datos.
 */
public interface Repositorio<T> {

    void guardar(T elemento);

    Optional<T> buscarPorId(String id);

    List<T> listar();
}
