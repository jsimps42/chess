package dataaccess;

import model.GameData;
import java.util.HashSet;

public interface GameAccess {
    void createGame(GameData game) throws Exception;
    GameData getGame(int gameID) throws Exception;
    boolean gameExists(int gameID) throws Exception;
    void updateGame(GameData game) throws Exception;
    HashSet<GameData> listGames() throws Exception;
    void clear() throws Exception;
}