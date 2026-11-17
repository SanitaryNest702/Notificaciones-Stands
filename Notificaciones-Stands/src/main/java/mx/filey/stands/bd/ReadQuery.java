package mx.filey.stands.bd;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReadQuery implements AutoCloseable {
    private final Connection conexion;
    private PreparedStatement pstmt;
    private ResultSet rs;

    public ReadQuery(Connection conexion) {
        if (conexion == null) {
            throw new IllegalArgumentException("La conexión no puede ser nula.");
        }
        this.conexion = conexion;
    }

    public ResultSet ejecutarConsulta(String sql, Object... parametros) throws SQLException {
        this.pstmt = conexion.prepareStatement(sql);
        if (parametros != null) {
            for (int i = 0; i < parametros.length; i++) {
                pstmt.setObject(i + 1, parametros[i]);
            }
        }
        this.rs = pstmt.executeQuery();
        return this.rs;
    }

    @Override
    public void close() {
        try {
            if (rs != null) rs.close();
        } catch (SQLException ignored) {}
        try {
            if (pstmt != null) pstmt.close();
        } catch (SQLException ignored) {}
    }
}