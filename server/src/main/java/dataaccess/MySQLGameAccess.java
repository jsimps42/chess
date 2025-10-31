package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import java.sql.*;
import java.util.HashSet;
import chess.ChessGame;

public class MySQLGameAccess implements GameAccess {
    private final Gson gson = new Gson();

    @Override
public void createGame(GameData game) throws DataAccessException {
    String sql = """
        INSERT INTO game (game_state, name, white_username, black_username)
        VALUES (?, ?, ?, ?)
        """;

    try (var conn = DatabaseManager.getConnection();
         var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ChessGame chess = game.game();
            if (chess == null) {
                chess = new ChessGame();
            }
            chess.resetBoard();

            String json = gson.toJson(chess);
            if (json == null || json.trim().isEmpty()) {
                json = gson.toJson(new ChessGame());
            }

            ps.setString(1, json);
            ps.setString(2, game.gameName());
            ps.setString(3, game.whiteUsername());
            ps.setString(4, game.blackUsername());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Failed to insert game: no rows affected");
            }

            try (var rs = ps.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new DataAccessException("Failed to retrieve generated game ID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error creating game", e);
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
                    var gameJson = rs.getString("game_state");
                    var chessGame = gson.fromJson(gameJson, ChessGame.class);
                    return new GameData(chessGame,
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("white_username"),
                            rs.getString("black_username")
                    );
                }
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Error retrieving game from DB", e);
        }
    }

    @Override
    public boolean gameExists(int gameID) throws DataAccessException {
        return getGame(gameID) != null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = """
            UPDATE game
            SET game_state = ?, name = ?, white_username = ?, black_username = ?
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
                throw new DataAccessException("No game with id " + game.gameID());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new DataAccessException("Error updating game", e);
        }
    }

    @Override
    public HashSet<GameData> listGames() throws DataAccessException {
        String sql = "SELECT * FROM game";
        var games = new HashSet<GameData>();
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            while (rs.next()) {
                var chessGame = gson.fromJson(rs.getString("game_state"), ChessGame.class);
                var game = new GameData(
                        chessGame,
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("white_username"),
                        rs.getString("black_username"));
                games.add(game);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Error listing games", e);
        }
        return games;
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM game");
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException("Failed to clear game table", e);
        }
    }
}
