package com.australito.dao;

import com.australito.model.Categoria;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {

    public List<Categoria> listarTodas() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT id_categoria, nombre, activo FROM CATEGORIAS WHERE activo = TRUE ORDER BY nombre ASC";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Categoria buscarPorId(Long id) throws SQLException {
        String sql = "SELECT id_categoria, nombre, activo FROM CATEGORIAS WHERE id_categoria = ?";
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

    private Categoria mapear(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id_categoria");
        String nombre = rs.getString("nombre");
        return new Categoria(id, nombre, BigDecimal.ZERO);
    }
}
