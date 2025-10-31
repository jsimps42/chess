package dataaccess;

import model.AuthData;
import java.sql.*;
import java.util.UUID;

public class MySQLAuthAccess implements AuthAccess{
    @Override
    public void addAuth(AuthData authData) throws DataAccessException {
        String sql = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authData.authToken());
            ps.setString(2, authData.username());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Error inserting auth", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = "SELECT username FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(authToken, rs.getString("username"));
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Error retrieving auth", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = "DELETE FROM auth WHERE authToken = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, authToken);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Auth token not found for deletion");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Error deleting auth", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
            var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM auth");
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Failed to clear tables", e);
        }
    }
}