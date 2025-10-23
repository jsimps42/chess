package dataaccess;

import model.GameData;
import java.util.HashSet;

public interface GameAccess {
    void createGame(GameData game);
    GameData getGame(int gameID) throws DataAccessException;
    boolean gameExists(int gameID);
    void updateGame(GameData game);
    HashSet<GameData> listGames();
    void clear();
}