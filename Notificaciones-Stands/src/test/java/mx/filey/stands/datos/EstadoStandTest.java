package mx.filey.stands.datos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class EstadoStandTest {

    /** Tabla completa de transiciones: filas = estado actual, columnas = estado nuevo. */
    @Test
    void soloSePermitenLasTransicionesDeLasReglasDeNegocio() {
        // orden: DISPONIBLE, RESERVADO, OCUPADO, BLOQUEADO
        boolean[][] permitido = {
            /* DISPONIBLE */ {false, true,  false, true},
            /* RESERVADO  */ {true,  false, true,  false},
            /* OCUPADO    */ {false, false, false, false},
            /* BLOQUEADO  */ {true,  true,  false, false},
        };
        EstadoStand[] estados = EstadoStand.values();
        assertEquals(4, estados.length);

        for (int desde = 0; desde < estados.length; desde++) {
            for (int hacia = 0; hacia < estados.length; hacia++) {
                assertEquals(permitido[desde][hacia], estados[desde].puedePasarA(estados[hacia]),
                    estados[desde] + " -> " + estados[hacia]);
            }
        }
    }
}
