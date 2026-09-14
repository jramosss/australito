package com.australito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    // Para entornos locales de prueba
    private static final String URL = "jdbc:mysql://localhost:3306/australito_db";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // Ajustar si hay contraseña

    public static Connection getConnection() throws SQLException {
        // En un entorno de producción, esto iría gestionado por un Pool de Conexiones
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
