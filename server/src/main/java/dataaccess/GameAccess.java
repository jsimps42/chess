package dataaccess;

import model.GameData;
import java.util.HashSet;

public interface GameAccess {
    void createGame(GameData game) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    boolean gameExists(int gameID) throws DataAccessException;
    void updateGame(GameData game) throws DataAccessException;
    HashSet<GameData> listGames() throws DataAccessException;
    void clear() throws DataAccessException;
}