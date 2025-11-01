package dataaccess;

import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static boolean tablesCreated = false;
    private static final Object LOCK = new Object();
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    public static void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("failed to create database", ex);
        }
    }

    public static void createTables() throws DataAccessException {
        synchronized (LOCK) {
            if (tablesCreated) {
                System.out.println("[DB] Tables already created – skipping");
                return;
            }

            try (var conn = getConnection();
                 var stmt = conn.createStatement()) {

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS user (
                        username VARCHAR(255) NOT NULL PRIMARY KEY,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255)
                    )
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS auth (
                        authToken VARCHAR(255) NOT NULL PRIMARY KEY,
                        username VARCHAR(255) NOT NULL
                    )
                """);

                stmt.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS game (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        game_state TEXT NOT NULL,
                        name VARCHAR(255) NOT NULL,
                        white_username VARCHAR(255),
                        black_username VARCHAR(255)
                    )
                """);

                try { stmt.executeUpdate("ALTER TABLE auth DROP FOREIGN KEY IF EXISTS fk_auth_user;"); } catch (SQLException ignored) {}
                try { stmt.executeUpdate("ALTER TABLE game DROP FOREIGN KEY IF EXISTS fk_game_white;"); } catch (SQLException ignored) {}
                try { stmt.executeUpdate("ALTER TABLE game DROP FOREIGN KEY IF EXISTS fk_game_black;"); } catch (SQLException ignored) {}

                stmt.executeUpdate("""
                    ALTER TABLE auth
                    ADD CONSTRAINT fk_auth_user
                    FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE
                    """);

                stmt.executeUpdate("""
                    ALTER TABLE game
                    ADD CONSTRAINT fk_game_white
                    FOREIGN KEY (white_username) REFERENCES user(username) ON DELETE SET NULL
                    """);

                stmt.executeUpdate("""
                    ALTER TABLE game
                    ADD CONSTRAINT fk_game_black
                    FOREIGN KEY (black_username) REFERENCES user(username) ON DELETE SET NULL
                    """);

            } catch (SQLException e) {
                throw new DataAccessException("Failed to create tables: " + e.getMessage(), e);
            } finally {
                tablesCreated = true;
            }

            System.out.println("[DB] Tables and constraints created (idempotent)");
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            DriverManager.setLogWriter(new PrintWriter(System.out));
            return conn;
        } catch (SQLException ex) {
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    public static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new RuntimeException("db.properties not found");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load db.properties", ex);
        }
    }

    public static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername   = props.getProperty("db.user");
        dbPassword   = props.getProperty("db.password");
        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }

    public static void resetTablesCreated() {
        synchronized (LOCK) {
            tablesCreated = false;
        }
    }
}