package dataaccess;

import model.AuthData;
import java.sql.*;
import static dataaccess.MySQLHelper.executeUpdate;
import static dataaccess.MySQLHelper.configureDatabase;

public class MySQLAuthAccess implements AuthAccess {
    private final String[] createAuthStatement = {
        """
        CREATE TABLE IF NOT EXISTS auth (
            authToken VARCHAR(255) NOT NULL,                
            username VARCHAR(255) NOT NULL,
            PRIMARY KEY (authToken)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    public MySQLAuthAccess() throws DataAccessException {
        configureDatabase(createAuthStatement);
    }

    @Override
    public void addAuth(AuthData authData) throws Exception {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        executeUpdate(statement, authData.authToken(), authData.username());
    }

    @Override
    public AuthData getAuth(String authToken) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(
                          rs.getString(authToken), 
                          rs.getString("username")
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error retrieving auth: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws Exception {
        try {
            String statement = "DELETE FROM auth WHERE token=?";
            executeUpdate(statement, authToken);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error deleting auth: %s", e));
        }
    }

    @Override
    public void clear() throws Exception {
        var statement = "TRUNCATE auth";
        executeUpdate(statement);
    }
}