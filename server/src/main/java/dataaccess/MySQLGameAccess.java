package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import java.sql.*;
import java.util.HashSet;
import chess.ChessGame;

public class MySQLGameAccess implements GameAccess {
    private final Gson gson = new Gson();

    @Override
    public int createGame(GameData game) throws DataAccessException {
        String sql = """
            INSERT INTO game (game_state, name, white_username, black_username)
            VALUES (?, ?, ?, ?)
            """;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, gson.toJson(game.game()));
            ps.setString(2, game.gameName());
            ps.setString(3, game.whiteUsername());
            ps.setString(4, game.blackUsername());

            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new DataAccessException("No ID generated");
        } catch (SQLException e) {
            throw new DataAccessException("SQL Error creating game: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT * FROM game WHERE id = ?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    ChessGame chessGame = gson.fromJson(rs.getString("game_state"), ChessGame.class);
                    return new GameData(
                        chessGame,
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("white_username"),
                        rs.getString("black_username")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error getting game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean gameExists(int gameID) throws DataAccessException {
        return getGame(gameID) != null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = """
            UPDATE game SET game_state = ?, name = ?, white_username = ?, black_username = ?
            WHERE id = ?
            """;
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {

            ps.setString(1, gson.toJson(game.game()));
            ps.setString(2, game.gameName());
            ps.setString(3, game.whiteUsername());
            ps.setString(4, game.blackUsername());
            ps.setInt(5, game.gameID());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Game not found: " + game.gameID());
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    @Override
    public HashSet<GameData> listGames() throws DataAccessException {
        HashSet<GameData> games = new HashSet<>();
        String sql = "SELECT * FROM game";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                ChessGame chessGame = gson.fromJson(rs.getString("game_state"), ChessGame.class);
                games.add(new GameData(
                    chessGame,
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("white_username"),
                    rs.getString("black_username")
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM game");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to clear games: " + e.getMessage());
        }
    }
}