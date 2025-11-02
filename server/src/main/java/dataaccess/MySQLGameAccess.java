package dataaccess;

import model.GameData;
import java.sql.*;
import java.util.HashSet;

public class MySQLGameAccess implements GameAccess {
    private final Gson gson = new Gson();

    @Override
    public void createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO game (name, white_username, black_username, game_state) VALUES (?, ?, ?, ?)";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, game.gameName());
            ps.setString(2, game.whiteUsername());
            ps.setString(3, game.blackUsername());
            ps.setString(4, gson.toJson(game.game()));
            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    game.setGameID(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error inserting game into DB", e);
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
                    var chessGame = gson.fromJson(gameJson, model.ChessGame.class);
                    return new GameData(
                            chessGame,
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("white_username"),
                            rs.getString("black_username")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game from DB", e);
        }
    }

    @Override
    public boolean gameExists(int gameID) throws DataAccessException {
        return getGame(gameID) != null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE game SET name=?, white_username=?, black_username=?, game_state=? WHERE id=?";
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(sql)) {
            ps.setString(1, game.name());
            ps.setString(2, game.whiteUsername());
            ps.setString(3, game.blackUsername());
            ps.setString(4, gson.toJson(game.game()));
            ps.setInt(5, game.gameID());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game in DB", e);
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
                var chessGame = gson.fromJson(rs.getString("game_state"), model.ChessGame.class);
                var game = new GameData(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        chessGame
                );
                games.add(game);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games", e);
        }
        return games;
    }

    @Override
    public void clear() throws DataAccessException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clear'");
    }
    
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  pet (
                game TEXT NOT NULL,
                gameID int NOT NULL AUTO_INCREMENT,
                gameName VARCHAR(255) NOT NULL,
                whiteUsername VARCHAR(255),
                whiteUsername VARCHAR(255)               
                PRIMARY KEY (gameID),
                INDEX(gameName),
                INDEX(whiteUsername)
                INDEX(blackUsername)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };
}
