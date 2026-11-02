package mx.filey.stands.datos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RepositorioEnMemoriaTest {
    private Repositorio<Stand> repositorio;

    @BeforeEach
    void preparar() {
        repositorio = new RepositorioEnMemoria<>(Stand::getId);
    }

    @Test
    void guardaYRecuperaPorIdentificador() {
        Stand s = new Stand("A-1", 10, BigDecimal.TEN);
        repositorio.guardar(s);
        assertEquals(s, repositorio.buscarPorId("A-1").get());
    }

    @Test
    void buscarUnIdInexistenteDevuelveVacio() {
        assertTrue(repositorio.buscarPorId("X-9").isEmpty());
    }

    @Test
    void listarConservaElOrdenDeInsercion() {
        repositorio.guardar(new Stand("B", 10, BigDecimal.TEN));
        repositorio.guardar(new Stand("A", 10, BigDecimal.TEN));
        repositorio.guardar(new Stand("C", 10, BigDecimal.TEN));

        List<Stand> lista = repositorio.listar();
        assertEquals("B", lista.get(0).getId());
        assertEquals("A", lista.get(1).getId());
        assertEquals("C", lista.get(2).getId());
    }

    @Test
    void guardarConElMismoIdReemplazaElElemento() {
        repositorio.guardar(new Stand("A", 10, BigDecimal.TEN));
        Stand nuevo = new Stand("A", 20, BigDecimal.TEN);
        repositorio.guardar(nuevo);

        assertEquals(1, repositorio.listar().size());
        assertEquals(20.0, repositorio.buscarPorId("A").get().getMetrosCuadrados());
    }

    @Test
    void modificarLaListaDevueltaNoAfectaAlRepositorio() {
        repositorio.guardar(new Stand("A", 10, BigDecimal.TEN));
        repositorio.listar().clear();
        assertEquals(1, repositorio.listar().size());
    }
}
