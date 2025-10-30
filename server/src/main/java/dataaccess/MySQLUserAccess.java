package dataaccess;

import model.UserData;
import java.sql.*;

import org.mindrot.jbcrypt.BCrypt;

public class MySQLUserAccess implements UserAccess{
    @Override
    public void addUser(UserData user) throws DataAccessException {
        String sql = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            String hashed = BCrypt.hashpw(user.password(), BCrypt.gensalt());

            ps.setString(1, user.username());
            ps.setString(2, hashed);
            ps.setString(3, user.email());
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new DataAccessException("Error inserting user into DB", e);
        }
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = "SELECT username, password, email FROM user WHERE username = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email"));
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving user from DB", e);
        }
    }

    @Override
    public boolean authenticateUser(String username, String password) throws DataAccessException {
        var user = getUser(username);
        if (user == null) {
            return false;
        }
        
        return BCrypt.checkpw(password, user.password());
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM user");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear user table", e);
        }
    }
}
