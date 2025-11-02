package dataaccess;

import model.AuthData;
import java.sql.*;

public class MySQLAuthAccess implements AuthAccess{
    @Override
    public void addAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (username, authToken) VALUES (?, ?)";
        var id = executeUpdate(statement, authData.username(), authData.authToken());
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException{
        String checkAuthSQL = "SELECT * FROM Auth WHERE authToken = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement checkAuthStatement = conn.prepareStatement(checkAuthSQL)) {
            checkAuthStatement.setString(1, authToken);
            ResultSet rs = checkAuthStatement.executeQuery();
            if (rs.next()) {
                String username = rs.getString("username");
                return new AuthData(authToken, username);
            }
        } catch (SQLException e) {
            System.out.println("SQL error: " + e.getMessage());
            throw new DataAccessException(e.getMessage());
        }
        System.out.println("AuthToken does not exist.");
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String deleteAuthSQL = "DELETE FROM Auth WHERE authToken = ?";
        try(Connection conn = DatabaseManager.getConnection();
            PreparedStatement deleteAuthStatement = conn.prepareStatement(deleteAuthSQL)) {
            deleteAuthStatement.setString(1, authToken);

            int rowsAffected = deleteAuthStatement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("AuthToken deleted successfully!");
            } else {
                System.out.println("Error: AuthToken deletion failed.");
                throw new NonSuccessException("AuthToken deletion failed.");
            }

        } catch (SQLException e) {
            System.out.println("Error during AuthToken deletion");
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }
    
    private final String[] createAuthAccess = {
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(255) NOT NULL,
                username VARCHAR(255) NOT NULL,
                PRIMARY KEY (authToken)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };
}
