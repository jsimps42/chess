package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static String databaseName;
    private static String dbUsername;
    private static String dbPassword;
    private static String connectionUrl;

    /*
     * Load the database information for the db.properties file.
     */
    static {
        loadPropertiesFromResources();
    }

    /**
     * Creates the database if it does not already exist.
     */
    static public void createDatabase() throws DataAccessException {
        var statement = "CREATE DATABASE IF NOT EXISTS " + databaseName;
        try (var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
             var preparedStatement = conn.prepareStatement(statement)) {
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.err.println("Error while creating the database: " + ex.getMessage());
            throw new DataAccessException("failed to create database", ex);
        }
    }

    public static void createTables() throws DataAccessException {
        try (var conn = getConnection()) {

            try (var stmt = conn.createStatement()) {
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
                        username VARCHAR(255) NOT NULL,
                        FOREIGN KEY (username) REFERENCES user(username)
                            ON DELETE CASCADE
                    )
                """);

                stmt.executeUpdate("DROP TABLE IF EXISTS game");
                stmt.executeUpdate("""
                    CREATE TABLE game (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        game_state TEXT NOT NULL,
                        name VARCHAR(255) NOT NULL,
                        white_username VARCHAR(255),
                        black_username VARCHAR(255),
                        FOREIGN KEY (white_username) REFERENCES user(username) ON DELETE SET NULL,
                        FOREIGN KEY (black_username) REFERENCES user(username) ON DELETE SET NULL
                    )
                """);
            }

        } catch (SQLException ex) {
           ex.printStackTrace();
            throw new DataAccessException("failed to create tables", ex);
        }
    }


    /**
     * Create a connection to the database and sets the catalog based upon the
     * properties specified in db.properties. Connections to the database should
     * be short-lived, and you must close the connection when you are done with it.
     * The easiest way to do that is with a try-with-resource block.
     * <br/>
     * <code>
     * try (var conn = DatabaseManager.getConnection()) {
     * // execute SQL statements.
     * }
     * </code>
     */
    static Connection getConnection() throws DataAccessException {
        try {
            //do not wrap the following line with a try-with-resources
            var conn = DriverManager.getConnection(connectionUrl, dbUsername, dbPassword);
            conn.setCatalog(databaseName);
            return conn;
        } catch (SQLException ex) {
            ex.printStackTrace();
            throw new DataAccessException("failed to get connection", ex);
        }
    }

    private static void loadPropertiesFromResources() {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new Exception("Unable to load db.properties");
            }
            Properties props = new Properties();
            props.load(propStream);
            loadProperties(props);
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException("unable to process db.properties", ex);
        }
    }

    private static void loadProperties(Properties props) {
        databaseName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.user");
        dbPassword = props.getProperty("db.password");

        var host = props.getProperty("db.host");
        var port = Integer.parseInt(props.getProperty("db.port"));
        connectionUrl = String.format("jdbc:mysql://%s:%d", host, port);
    }
}
