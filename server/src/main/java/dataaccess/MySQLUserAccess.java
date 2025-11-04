package dataaccess;

import model.UserData;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import static dataaccess.MySQLHelper.executeUpdate;
import static dataaccess.MySQLHelper.configureDatabase;

public class MySQLUserAccess implements UserAccess{
    private final String[] createUserStatement = {
        """
            CREATE TABLE IF NOT EXISTS  user (
                username VARCHAR(255) NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL,
                PRIMARY KEY (username),
                INDEX(email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    public MySQLUserAccess() throws DataAccessException {
        configureDatabase(createUserStatement);
    }

    @Override
    public void addUser(UserData user) throws DataAccessException {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
        executeUpdate(statement, user.username(), hashedPassword, user.email());
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM user WHERE username=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new UserData(
                          rs.getString(username), 
                          rs.getString("password"), 
                          rs.getString("email")
                        );
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error retrieving user: %s", e.getMessage()));
        }
        return null;
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        UserData userData = getUser(username);
        return BCrypt.checkpw(password, userData.password());
    }

    @Override
    public void clear() throws DataAccessException {
        var statement = "TRUNCATE user";
        executeUpdate(statement);
    }
}