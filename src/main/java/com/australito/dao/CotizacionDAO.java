package com.australito.dao;

import com.australito.model.Cotizacion;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CotizacionDAO {

    public Long guardar(Cotizacion cotizacion) throws SQLException {
        String sql = "INSERT INTO COTIZACION_HISTORICA (valor_compra, valor_venta, fecha_hora) VALUES (?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setBigDecimal(1, cotizacion.getValorCompra());
            ps.setBigDecimal(2, cotizacion.getValorVenta());
            LocalDateTime fecha = cotizacion.getFechaActualizacion() != null ? cotizacion.getFechaActualizacion() : LocalDateTime.now();
            ps.setTimestamp(3, Timestamp.valueOf(fecha));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    cotizacion.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public Cotizacion obtenerUltima() throws SQLException {
        // En modo degradado se recupera la tasa persistida más reciente para garantizar la continuidad operativa
        String sql = "SELECT id_cotizacion, valor_compra, valor_venta, fecha_hora FROM COTIZACION_HISTORICA ORDER BY id_cotizacion DESC LIMIT 1";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return mapear(rs);
            }
        }
        return null;
    }

    private Cotizacion mapear(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id_cotizacion");
        BigDecimal compra = rs.getBigDecimal("valor_compra");
        BigDecimal venta = rs.getBigDecimal("valor_venta");
        Timestamp ts = rs.getTimestamp("fecha_hora");
        Cotizacion cot = new Cotizacion(id, compra, venta, "Base MySQL");
        if (ts != null) {
            cot.setFechaActualizacion(ts.toLocalDateTime());
        }
        return cot;
    }
}
