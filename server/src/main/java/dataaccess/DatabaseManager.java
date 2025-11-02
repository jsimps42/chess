package dataaccess;

import java.io.PrintWriter;
import java.sql.*;
import java.util.Properties;
//delete this change
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

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername   = props.getProperty("db.user");
        dbPassword   = props.getProperty("db.password");
        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}