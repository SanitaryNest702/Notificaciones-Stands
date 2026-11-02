package mx.filey.stands.datos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Repositorio que guarda los elementos en un mapa, conservando el orden de inserción.
 * Los datos se pierden al cerrar el programa.
 */
public class RepositorioEnMemoria<T> implements Repositorio<T> {
    private final Map<String, T> elementos = new LinkedHashMap<>();
    private final Function<T, String> extractorId;

    /**
     * @param extractorId función que obtiene el identificador de un elemento,
     *                    por ejemplo {@code Stand::getId}
     */
    public RepositorioEnMemoria(Function<T, String> extractorId) {
        this.extractorId = extractorId;
    }

    @Override
    public void guardar(T elemento) {
        elementos.put(extractorId.apply(elemento), elemento);
    }

    @Override
    public Optional<T> buscarPorId(String id) {
        return Optional.ofNullable(elementos.get(id));
    }

    @Override
    public List<T> listar() {
        return new ArrayList<>(elementos.values());
    }
}
