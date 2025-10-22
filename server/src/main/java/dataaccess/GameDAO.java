package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {
    int createGame(String gameName);
    Collection<GameData> listGames();
    GameData getGame(int gameID);
    String getBlackUser(int gameID);
    String getWhiteUser(int gameID);
    void updateGame(int gameID, GameData newGame);
    void clear();
}