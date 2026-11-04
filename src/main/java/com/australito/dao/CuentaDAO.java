package com.australito.dao;

import com.australito.model.Cuenta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CuentaDAO {

    public List<Cuenta> listarTodas() throws SQLException {
        List<Cuenta> lista = new ArrayList<>();
        String sql = "SELECT id_cuenta, denominacion, tipo FROM CUENTAS ORDER BY id_cuenta ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Cuenta buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id_cuenta, denominacion, tipo FROM CUENTAS WHERE id_cuenta = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapear(rs);
                }
            }
        }
        return null;
    }

    private Cuenta mapear(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id_cuenta");
        String denominacion = rs.getString("denominacion");
        String tipo = rs.getString("tipo");
        return new Cuenta(id, denominacion, tipo, BigDecimal.ZERO);
    }
}
