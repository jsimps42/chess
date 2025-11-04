package dataaccess;

import dataaccess.DataAccessException;
import model.GameData;

import java.util.HashSet;

public class MemoryGameAccess implements GameAccess {
    private HashSet<GameData> db;

    public MemoryGameAccess() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public HashSet<GameData> listGames() {
        return db;
    }

    @Override
    public void createGame(GameData game) throws DataAccessException {
        db.add(game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData game : db) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new DataAccessException("Game does not exist: " + gameID);
    }

    @Override
    public boolean gameExists(int gameID) {
        for (GameData game : db) {
            if (game.gameID() == gameID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try {
            db.remove(getGame(game.gameID()));
            db.add(game);
        }
        catch (DataAccessException e) {
            db.add(game);
        }
    }

    @Override
    public void clear() throws Exception {
        db = new HashSet<>(16);
    }
}