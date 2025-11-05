package dataaccess;

import model.GameData;
import java.sql.*;
import java.util.HashSet;

import com.google.gson.Gson;

import chess.ChessGame;

import static dataaccess.MySQLHelper.executeUpdate;
import static dataaccess.MySQLHelper.configureDatabase;

public class MySQLGameAccess implements GameAccess{
    private final String[] createGameStatement = {
        """
        CREATE TABLE IF NOT EXISTS game (
            chessGame TEXT NOT NULL,
            gameID int NOT NULL,
            gameName VARCHAR(255) NOT NULL,
            whiteUsername VARCHAR(255),
            blackUsername VARCHAR(255),               
            PRIMARY KEY (gameID),
            INDEX(gameName),
            INDEX(whiteUsername),
            INDEX(blackUsername)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    public MySQLGameAccess() throws DataAccessException {
        configureDatabase(createGameStatement);
    }

    @Override
    public void createGame(GameData game) throws Exception {
        if (getGame(game.gameID()) != null) {
            throw new DataAccessException("Error: game with gameID " + game.gameID() + " already exists");        
        }
        try {
            executeUpdate(
                "INSERT INTO game (chessGame, gameId, gameName, whiteUsername, blackUsername) VALUES (?, ?, ?, ? ,?)", 
                new Gson().toJson(game.game()), 
                game.gameID(), 
                game.gameName(), 
                game.whiteUsername(), 
                game.blackUsername());
        } catch (DataAccessException e) {
            throw new DataAccessException(String.format("Error creating game: %s", e.getMessage()));
        }    
    }

    @Override
    public GameData getGame(int gameID) throws Exception {
        String statement = "SELECT chessGame, gameId, gameName, whiteUsername, blackUsername FROM game WHERE gameId=?";
        try (Connection conn = DatabaseManager.getConnection();
          PreparedStatement ps = conn.prepareStatement(statement)) {
            ps.setInt(1, gameID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GameData(
                      new Gson().fromJson(rs.getString("chessGame"), ChessGame.class), 
                      rs.getInt("gameId"), 
                      rs.getString("gameName"), 
                      rs.getString("whiteUsername"), 
                      rs.getString("blackUsername")
                    );
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error retrieving game: %s", e));
        }
        return null;
    }

    @Override
    public boolean gameExists(int gameID) throws Exception {
        return getGame(gameID) != null;
    }

    @Override
    public void updateGame(GameData game) throws Exception {
        if (getGame(game.gameID()) == null) {
            throw new DataAccessException("Error: game with gameID " + game.gameID() + " does not exist");        
        }

        executeUpdate(
          "UPDATE game SET chessGame=?, gameName=?, whiteUsername=?, blackUsername=? WHERE gameId=?", 
          new Gson().toJson(game.game()),
          game.gameName(),  
          game.whiteUsername(), 
          game.blackUsername(), 
          game.gameID()
        );
    }

    @Override
    public HashSet<GameData> listGames() throws Exception {
        HashSet<GameData> result = new HashSet<>(16);
        try (Connection conn = DatabaseManager.getConnection()) {
            String statement = "SELECT chessGame, gameId, gameName, whiteUsername, blackUsername FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(new GameData(
                          new Gson().fromJson(rs.getString("chessGame"), ChessGame.class), 
                          rs.getInt("gameId"), 
                          rs.getString("gameName"), 
                          rs.getString("whiteUsername"), 
                          rs.getString("blackUsername")
                        ));
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw new DataAccessException(String.format("Error retrieving games: %s", e.getMessage()));
        }    
    }

    @Override
    public void clear() throws Exception {
        var statement = "TRUNCATE game";
        executeUpdate(statement);
    }
}