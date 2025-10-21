package dataaccess;

import chess.ChessGame;
import model.GameData;
import java.util.Collection;
import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{

    private final HashMap<Integer, GameData> games = new HashMap<>();
    private int gameID = 1;

    @Override
    public void clear(){
        games.clear();
    }

    @Override
    public int createGame(String gameName) {
        GameData game = new GameData(new ChessGame(), gameID, gameName, null, null);
        gameID++;
        games.put(gameID, game);
        return gameID;
    }

    @Override
    public String getBlackUser(String gameID) {
        GameData game = games.get(Integer.parseInt(gameID));
        return game.blackUsername();
    }

    @Override
    public String getWhiteUser(String gameId) {
        GameData game = games.get(Integer.parseInt(gameId));
        return game.whiteUsername();
    }

    @Override
    public GameData getGame(String gameId) {
        return games.get(Integer.parseInt(gameId));
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public void updateGame(String gameId, GameData newGame) {
        games.put(Integer.parseInt(gameId), newGame);
    }

}