package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO {

    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int nextGameID = 1;

    @Override
    public void clear() {
        games.clear();
        nextGameID = 1;
    }

    @Override
    public int createGame(String gameName) {
        int currentID = nextGameID++;
        GameData game = new GameData(new ChessGame(), currentID, gameName, null, null);
        games.put(currentID, game);
        return currentID;
    }

    @Override
    public GameData getGame(int gameID) {
        return games.get(gameID);
    }

    @Override
    public String getBlackUser(int gameID) {
        GameData game = games.get(gameID);
        return game != null ? game.blackUsername() : null;
    }

    @Override
    public String getWhiteUser(int gameID) {
        GameData game = games.get(gameID);
        return game != null ? game.whiteUsername() : null;
    }

    @Override
    public void updateGame(int gameID, GameData newGame) {
        games.put(gameID, newGame);
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }
}