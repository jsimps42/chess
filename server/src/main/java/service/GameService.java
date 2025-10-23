package service;

import dataaccess.GameDAO;
import model.GameData;
import java.util.Collection;

public class GameService {
    private final GameDAO gameDAO;

    public GameService(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    public GameData createGame(String gameName) {
        if (gameName == null || gameName.isBlank()) {
            return null;
        }
        
        int id = gameDAO.createGame(gameName);
        return gameDAO.getGame(id);
    }  


    public Collection<GameData> listGames() {
        return gameDAO.listGames();
    }

    public GameData joinGame(int gameID, String username) throws Exception {
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new Exception("game not found");
        }

        String whiteUser = game.whiteUsername();
        String blackUser = game.blackUsername();

        if (whiteUser == null) {
            whiteUser = username;
        } else if (blackUser == null && !username.equals(whiteUser)) {
            blackUser = username;
        } else {
            throw new Exception("game full or user already joined");
        }

        GameData updatedGame = new GameData(game.game(), game.gameID(), game.gameName(), whiteUser, blackUser);
        gameDAO.updateGame(gameID, updatedGame);
        return updatedGame;
    }

    public GameData getGame(int id) {
        return gameDAO.getGame(id);
    }

    public void updateGame(GameData game) {
        gameDAO.updateGame(game.gameID(), game);
    }

}