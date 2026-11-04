package com.australito.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {
    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DATABASE = "australito_db";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String host = DEFAULT_HOST;
    private static String port = DEFAULT_PORT;
    private static String database = DEFAULT_DATABASE;
    private static String user = DEFAULT_USER;
    private static String password = DEFAULT_PASSWORD;

    static {
        String envHost = System.getenv("AUSTRALITO_DB_HOST");
        if (envHost != null && !envHost.isBlank()) {
            host = envHost;
        }
        String envPort = System.getenv("AUSTRALITO_DB_PORT");
        if (envPort != null && !envPort.isBlank()) {
            port = envPort;
        }
        String envDatabase = System.getenv("AUSTRALITO_DB_NAME");
        if (envDatabase != null && !envDatabase.isBlank()) {
            database = envDatabase;
        }
        String envUser = System.getenv("AUSTRALITO_DB_USER");
        if (envUser != null && !envUser.isBlank()) {
            user = envUser;
        }
        String envPassword = System.getenv("AUSTRALITO_DB_PASS");
        if (envPassword != null) {
            password = envPassword;
        }
    }

    private ConexionDB() {
    }

    public static Connection obtenerConexion() throws SQLException {
        String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=America/Argentina/Cordoba&allowPublicKeyRetrieval=true&connectTimeout=3000",
                host, port, database);
        return DriverManager.getConnection(url, user, password);
    }

    public static boolean probarConexion() {
        try (Connection conn = obtenerConexion()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
