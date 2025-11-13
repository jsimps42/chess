package dataaccess;

import java.util.HashSet;
import model.GameData;

public class MemoryGameAccess implements GameAccess{
    HashSet<GameData> db;
    public MemoryGameAccess() {
        db = new HashSet<>(16);
    }

    @Override
    public void createGame(GameData game) {
        db.add(game);
    }

    @Override
    public GameData getGame(int gameID) {
        for (GameData game : db) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        return null;
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
    public HashSet<GameData> listGames() {
        return db;
    }

    @Override
    public void updateGame(GameData game) {
        try {
            db.remove(getGame(game.gameID()));
            db.add(game);
        } catch (Exception e) {
            db.add(game);
        }
    }

    @Override
    public void clear() throws Exception {
        db = new HashSet<>(16);
    }
}