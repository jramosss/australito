package com.australito.dao;

import com.australito.model.Categoria;
import com.australito.model.Cotizacion;
import com.australito.model.Cuenta;
import com.australito.model.Gasto;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GastoDAO {

    public Long insertar(Gasto gasto) throws SQLException {
        // La sentencia parametrizada previene inyecciones SQL y garantiza el tipado estricto
        String sql = "INSERT INTO GASTOS (monto_ars, monto_usd, fecha_gasto, descripcion, id_categoria, id_cuenta, id_cotizacion) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setBigDecimal(1, gasto.getMonto());
            ps.setBigDecimal(2, gasto.getMontoUSD());

            LocalDate fecha = gasto.getFecha() != null ? gasto.getFecha() : LocalDate.now();
            ps.setTimestamp(3, Timestamp.valueOf(fecha.atStartOfDay()));
            ps.setString(4, gasto.getDescripcion());
            ps.setLong(5, gasto.getCategoria().getId());
            ps.setLong(6, gasto.getCuenta().getId());
            ps.setLong(7, gasto.getCotizacionAplicada().getId());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    gasto.setId(id);
                    return id;
                }
            }
        }
        return null;
    }

    public List<Gasto> listarTodos() throws SQLException {
        List<Gasto> lista = new ArrayList<>();
        String sql = "SELECT g.id_gasto, g.monto_ars, g.monto_usd, g.fecha_gasto, g.descripcion, " +
                     "c.id_categoria, c.nombre AS categoria_nombre, " +
                     "cu.id_cuenta, cu.denominacion AS cuenta_nombre, cu.tipo AS cuenta_tipo, " +
                     "cot.id_cotizacion, cot.valor_compra, cot.valor_venta, cot.fecha_hora AS cot_fecha " +
                     "FROM GASTOS g " +
                     "JOIN CATEGORIAS c ON g.id_categoria = c.id_categoria " +
                     "JOIN CUENTAS cu ON g.id_cuenta = cu.id_cuenta " +
                     "JOIN COTIZACION_HISTORICA cot ON g.id_cotizacion = cot.id_cotizacion " +
                     "ORDER BY g.fecha_gasto DESC, g.id_gasto DESC";

        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapearGastoCompleto(rs));
            }
        }
        return lista;
    }

    public boolean eliminar(Long idGasto) throws SQLException {
        String sql = "DELETE FROM GASTOS WHERE id_gasto = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idGasto);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizar(Gasto gasto) throws SQLException {
        // La actualización parametrizada asegura la integridad referencial y previene inyección SQL
        String sql = "UPDATE GASTOS SET monto_ars = ?, monto_usd = ?, fecha_gasto = ?, descripcion = ?, " +
                     "id_categoria = ?, id_cuenta = ?, id_cotizacion = ? WHERE id_gasto = ?";
        try (Connection conn = ConexionDB.obtenerConexion();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, gasto.getMonto());
            ps.setBigDecimal(2, gasto.getMontoUSD());

            LocalDate fecha = gasto.getFecha() != null ? gasto.getFecha() : LocalDate.now();
            ps.setTimestamp(3, Timestamp.valueOf(fecha.atStartOfDay()));
            ps.setString(4, gasto.getDescripcion());
            ps.setLong(5, gasto.getCategoria().getId());
            ps.setLong(6, gasto.getCuenta().getId());
            ps.setLong(7, gasto.getCotizacionAplicada().getId());
            ps.setLong(8, gasto.getId());

            return ps.executeUpdate() > 0;
        }
    }

    private Gasto mapearGastoCompleto(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id_gasto");
        BigDecimal montoArs = rs.getBigDecimal("monto_ars");
        BigDecimal montoUsd = rs.getBigDecimal("monto_usd");
        Timestamp tsFecha = rs.getTimestamp("fecha_gasto");
        LocalDate fecha = (tsFecha != null) ? tsFecha.toLocalDateTime().toLocalDate() : LocalDate.now();
        String descripcion = rs.getString("descripcion");

        Long idCat = rs.getLong("id_categoria");
        String nombreCat = rs.getString("categoria_nombre");
        Categoria categoria = new Categoria(idCat, nombreCat, BigDecimal.ZERO);

        Long idCta = rs.getLong("id_cuenta");
        String nombreCta = rs.getString("cuenta_nombre");
        String tipoCta = rs.getString("cuenta_tipo");
        Cuenta cuenta = new Cuenta(idCta, nombreCta, tipoCta, BigDecimal.ZERO);

        Long idCot = rs.getLong("id_cotizacion");
        BigDecimal compra = rs.getBigDecimal("valor_compra");
        BigDecimal venta = rs.getBigDecimal("valor_venta");
        Timestamp tsCot = rs.getTimestamp("cot_fecha");
        Cotizacion cotizacion = new Cotizacion(idCot, compra, venta, "Snapshot Historico");
        if (tsCot != null) {
            cotizacion.setFechaActualizacion(tsCot.toLocalDateTime());
        }

        Gasto gasto = new Gasto(id, fecha, descripcion, montoArs, categoria, cuenta, cotizacion);
        gasto.setMontoUSD(montoUsd);
        return gasto;
    }
}
