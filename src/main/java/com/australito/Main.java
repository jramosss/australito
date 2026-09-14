package com.australito;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Prototipo Australito: Ejecutando Pruebas de Base de Datos ===");
        
        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("-> Conexión a MySQL establecida con éxito.\n");

            // 1. Inserción (Simulando la conversión)
            double cotizacionSimulada = 1250.50;
            double montoArs = 20000.0;
            double montoUsd = montoArs / cotizacionSimulada;

            String sqlInsert = "INSERT INTO transacciones (cuenta_id, categoria_id, monto_ars, monto_usd, fecha, descripcion) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, 1); // MercadoPago
                stmt.setInt(2, 1); // Supermercado
                stmt.setDouble(3, montoArs);
                stmt.setDouble(4, montoUsd);
                stmt.setDate(5, new java.sql.Date(System.currentTimeMillis()));
                stmt.setString(6, "Compra semanal de prueba");
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("-> [INSERT] Transacción registrada con éxito.");
                }
            } catch (SQLException e) {
                System.out.println("Error al insertar: Verifique que existan las FK correspondientes (cuenta_id 1, categoria_id 1).");
            }

            // 2. Consulta
            String sqlSelect = "SELECT * FROM transacciones";
            List<Transaccion> transacciones = new ArrayList<>();
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlSelect)) {
                
                while (rs.next()) {
                    Transaccion t = new Transaccion(
                        rs.getInt("cuenta_id"),
                        rs.getInt("categoria_id"),
                        rs.getDouble("monto_ars"),
                        rs.getDouble("monto_usd"),
                        rs.getDate("fecha"),
                        rs.getString("descripcion")
                    );
                    t.setId(rs.getInt("id"));
                    transacciones.add(t);
                }
            }
            
            System.out.println("\n-> [SELECT] Transacciones almacenadas:");
            for (Transaccion t : transacciones) {
                System.out.println(t);
            }

            // 3. Borrado
            if (!transacciones.isEmpty()) {
                int idABorrar = transacciones.get(transacciones.size() - 1).getId();
                String sqlDelete = "DELETE FROM transacciones WHERE id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sqlDelete)) {
                    stmt.setInt(1, idABorrar);
                    int deleted = stmt.executeUpdate();
                    System.out.println("\n-> [DELETE] Transacciones borradas: " + deleted + " (ID: " + idABorrar + ")");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error de base de datos: " + e.getMessage());
            System.out.println("Nota: Este prototipo requiere tener MySQL corriendo con la base de datos 'australito_db' creada (ver schema.sql y data.sql).");
        }
    }
}
