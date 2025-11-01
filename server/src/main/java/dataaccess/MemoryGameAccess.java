package dataaccess;

import model.GameData;
import java.util.*;

public class MemoryGameAccess implements GameAccess {
    private final HashMap<Integer, GameData> db = new HashMap<>();
    private int nextId = 1;

    @Override
    public int createGame(GameData game) throws DataAccessException {
        int id = nextId++;
        GameData g = new GameData(
                game.game(),
                id,
                game.gameName(),
                game.whiteUsername(),
                game.blackUsername()
        );
        db.put(id, g);
        return id;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData g = db.get(gameID);
        if (g == null) {
            throw new DataAccessException("Game not found, id: " + gameID);
        }
        return g;
    }

    @Override
    public boolean gameExists(int gameID) throws DataAccessException {
        return db.containsKey(gameID);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (!db.containsKey(game.gameID())) {
            throw new DataAccessException("Game not found, id: " + game.gameID());
        }
        db.put(game.gameID(), game);
    }

    @Override
    public HashSet<GameData> listGames() throws DataAccessException {
        return new HashSet<>(db.values());
    }

    @Override
    public void clear() throws DataAccessException {
        db.clear();
        nextId = 1;
    }
}