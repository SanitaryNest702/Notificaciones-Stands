package mx.filey.stands.bd;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConexionBD {

    public static final String URL_RESPALDO = "jdbc:sqlite:stands.db";
    private static final String ARCHIVO_PROPIEDADES = "bd.properties";

    private final String url;
    private final String usuario;
    private final String password;

    public ConexionBD(String url, String usuario, String password) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("La URL de la base de datos no puede estar vacia");
        }
        this.url = url;
        this.usuario = usuario;
        this.password = password;
    }

    public static ConexionBD cargar() {
        String url = limpio(System.getenv("DB_URL"));
        String usuario = limpio(System.getenv("DB_USUARIO"));
        String password = limpio(System.getenv("DB_PASSWORD"));

        if (url == null) {
            Properties archivo = leerArchivo(Path.of(ARCHIVO_PROPIEDADES));
            url = limpio(archivo.getProperty("url"));
            usuario = limpio(archivo.getProperty("usuario"));
            password = limpio(archivo.getProperty("password"));
        }
        if (url == null) {
            return new ConexionBD(URL_RESPALDO, null, null);
        }
        return desdeValores(url, usuario, password);
    }

    public static ConexionBD desdeValores(String url, String usuario, String password) {
        if (url.startsWith("postgresql://") || url.startsWith("postgres://")) {
            return convertirUrlPostgres(url, usuario, password);
        }
        return new ConexionBD(url, usuario, password);
    }

    private static ConexionBD convertirUrlPostgres(String original, String usuario, String password) {
        URI uri;
        try {
            uri = new URI(original);
        } catch (URISyntaxException e) {
            throw new ErrorBaseDatos("La URL de la base de datos no es valida", e);
        }
        String user = usuario;
        String clave = password;
        if (uri.getUserInfo() != null) {
            String[] partes = uri.getUserInfo().split(":", 2);
            if (user == null) {
                user = partes[0];
            }
            if (clave == null && partes.length > 1) {
                clave = partes[1];
            }
        }
        StringBuilder jdbc = new StringBuilder("jdbc:postgresql://").append(uri.getHost());
        if (uri.getPort() != -1) {
            jdbc.append(':').append(uri.getPort());
        }
        jdbc.append(uri.getPath() == null ? "" : uri.getPath());
        String consulta = quitarParametro(uri.getRawQuery(), "channel_binding");
        if (consulta != null && !consulta.isEmpty()) {
            jdbc.append('?').append(consulta);
        }
        return new ConexionBD(jdbc.toString(), user, clave);
    }

    private static String quitarParametro(String consulta, String nombre) {
        if (consulta == null) {
            return null;
        }
        StringBuilder resultado = new StringBuilder();
        for (String parametro : consulta.split("&")) {
            if (parametro.startsWith(nombre + "=")) {
                continue;
            }
            if (resultado.length() > 0) {
                resultado.append('&');
            }
            resultado.append(parametro);
        }
        return resultado.toString();
    }

    private static Properties leerArchivo(Path ruta) {
        Properties propiedades = new Properties();
        if (Files.exists(ruta)) {
            try (InputStream entrada = Files.newInputStream(ruta)) {
                propiedades.load(entrada);
            } catch (IOException e) {
                throw new ErrorBaseDatos("No se pudo leer " + ARCHIVO_PROPIEDADES, e);
            }
        }
        return propiedades;
    }

    private static String limpio(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }

    public Connection abrir() {
        try {
            if (usuario == null) {
                return DriverManager.getConnection(url);
            }
            return DriverManager.getConnection(url, usuario, password);
        } catch (SQLException e) {
            throw new ErrorBaseDatos("No se pudo conectar a " + getUrlSegura() + ": " + e.getMessage(), e);
        }
    }

    String getUsuario() { return usuario; }
    String getPassword() { return password; }

    public boolean esSQLite() {
        return url.startsWith("jdbc:sqlite:");
    }

    public String getDescripcion() {
        if (esSQLite()) {
            return "SQLite (archivo local)";
        }
        if (url.startsWith("jdbc:postgresql:")) {
            return "PostgreSQL";
        }
        return "Base de datos JDBC";
    }

    public String getUrlSegura() {
        return url.replaceAll("(?i)(password=)[^&]*", "$1***");
    }
}