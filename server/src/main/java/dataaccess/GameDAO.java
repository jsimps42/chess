package dataaccess;

import model.GameData;
import java.util.Collection;

public interface GameDAO {
    int createGame(String gameName) throws DataAccessException;
    Collection<GameData> listGames() throws DataAccessException;
    GameData getGame(String gameID) throws DataAccessException;
    String getBlackUser(String gameID) throws DataAccessException;
    String getWhiteUser(String gameID) throws DataAccessException;
    void updateGame(String gameID, GameData newGame) throws DataAccessException;
    void clear();
}