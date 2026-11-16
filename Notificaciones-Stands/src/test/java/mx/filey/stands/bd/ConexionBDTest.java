package mx.filey.stands.bd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de la configuración de la conexión y de la lectura de scripts.
 * No necesitan base de datos: solo verifican cómo se interpreta la configuración.
 */
class ConexionBDTest {

    private static final String URL_NEON =
        "postgresql://usuario1:cl4ve@ep-cool-123.us-east-2.aws.neon.tech/neondb"
        + "?sslmode=require&channel_binding=require";

    @Test
    void laUrlDeNeonSeConvierteAFormatoJdbc() {
        ConexionBD bd = ConexionBD.desdeValores(URL_NEON, null, null);

        assertEquals("jdbc:postgresql://ep-cool-123.us-east-2.aws.neon.tech/neondb?sslmode=require",
            bd.getUrlSegura());
        assertEquals("usuario1", bd.getUsuario());
        assertEquals("cl4ve", bd.getPassword());
        assertEquals("PostgreSQL", bd.getDescripcion());
    }

    @Test
    void elUsuarioYClaveExplicitosTienenPrioridadSobreLosDeLaUrl() {
        ConexionBD bd = ConexionBD.desdeValores(URL_NEON, "otro", "otraClave");

        assertEquals("otro", bd.getUsuario());
        assertEquals("otraClave", bd.getPassword());
    }

    @Test
    void laUrlMostradaNuncaIncluyeLaClave() {
        ConexionBD desdeNeon = ConexionBD.desdeValores(URL_NEON, null, null);
        ConexionBD jdbc = ConexionBD.desdeValores(
            "jdbc:postgresql://host:5432/base?user=x&password=secreto&sslmode=require", null, null);

        assertFalse(desdeNeon.getUrlSegura().contains("cl4ve"));
        assertTrue(jdbc.getUrlSegura().contains("password=***"));
        assertFalse(jdbc.getUrlSegura().contains("secreto"));
    }

    @Test
    void unaUrlJdbcSeRespetaTalCual() {
        ConexionBD bd = ConexionBD.desdeValores("jdbc:postgresql://host:5432/base", "u", "p");

        assertEquals("jdbc:postgresql://host:5432/base", bd.getUrlSegura());
        assertEquals("u", bd.getUsuario());
    }

    @Test
    void sqliteSeReconoceComoRespaldoLocal() {
        ConexionBD bd = new ConexionBD(ConexionBD.URL_RESPALDO, null, null);

        assertTrue(bd.esSQLite());
        assertEquals("SQLite (archivo local)", bd.getDescripcion());
    }

    @Test
    void laUrlVaciaSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> new ConexionBD(" ", null, null));
        assertThrows(IllegalArgumentException.class, () -> new ConexionBD(null, null, null));
    }

    @Test
    void sinDriverOServidorLaConexionFallaConUnErrorClaro() {
        ConexionBD bd = new ConexionBD("jdbc:no-existe://nada", null, null);

        ErrorBaseDatos error = assertThrows(ErrorBaseDatos.class, bd::abrir);
        assertTrue(error.getMessage().startsWith("No se pudo conectar"));
    }

    @Test
    void elScriptSeSeparaEnSentenciasIgnorandoComentarios() {
        String script = "-- comentario; con punto y coma\n"
            + "CREATE TABLE a (id INT);\n"
            + "\n"
            + "-- otro comentario\n"
            + "INSERT INTO a VALUES (1),\n (2);\n";

        List<String> sentencias = InicializadorBD.separarSentencias(script);

        assertEquals(2, sentencias.size());
        assertEquals("CREATE TABLE a (id INT)", sentencias.get(0));
        assertTrue(sentencias.get(1).startsWith("INSERT INTO a VALUES (1),"));
    }

    @Test
    void losScriptsIncluidosTienenLasTresTablasYSusDatos() {
        // Se leen desde los recursos del proyecto: schema.sql crea 3 tablas y datos.sql inserta en las 3.
        List<String> esquema = InicializadorBD.separarSentencias(leer("/bd/schema.sql"));
        List<String> datos = InicializadorBD.separarSentencias(leer("/bd/datos.sql"));

        assertEquals(3, esquema.size());
        assertEquals(3, datos.size());
        assertTrue(esquema.get(0).contains("stands"));
        assertTrue(esquema.get(1).contains("editoriales"));
        assertTrue(esquema.get(2).contains("reservaciones"));
    }

    private static String leer(String ruta) {
        try (java.io.InputStream entrada = ConexionBDTest.class.getResourceAsStream(ruta)) {
            return new String(entrada.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
