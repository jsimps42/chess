package service;

import dataaccess.*;
import model.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;

public class GameService {
    GameAccess gameAccess;
    AuthAccess authAccess;

    public GameService(GameAccess gameAccess, AuthAccess authAccess) {
        this.gameAccess = gameAccess;
        this.authAccess = authAccess;
    }

    public HashSet<GameData> listGames(String authToken) throws Exception {
        if (authAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException();
        }
        return gameAccess.listGames();
    }

    public int createGame(String authToken, String gameName) throws Exception {
        authAccess.getAuth(authToken);
        Random rand = new Random();
        int gameID;
        do {
            gameID = rand.nextInt(9999) + 1;
        } while (gameAccess.gameExists(gameID));

        gameAccess.createGame(new GameData(null, gameID, gameName, null, null));

        return gameID;
    }

    public boolean joinGame(String authToken, int gameID, String color) throws Exception {

        AuthData authData;
        GameData gameData;
        authData = authAccess.getAuth(authToken);
        if (authData == null) {
            throw new UnauthorizedException();
        }
        
        gameData = gameAccess.getGame(gameID);

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();

        if (Objects.equals(color, "WHITE")) {
            if (whiteUser != null) {
                return false;
            } else {
                whiteUser = authData.username();
            }
        } else if (Objects.equals(color, "BLACK")) {
            if (blackUser != null) {
                return false;
            } else {
                blackUser = authData.username();
            }
        } else if (color == null || color.isEmpty()) {
            throw new BadRequestException("Error: Player color cannot be null or empty");
        } else {
            throw new BadRequestException("Error: %s is not a valid team color".formatted(color));
        }
        gameAccess.updateGame(new GameData(gameData.game(), gameID, gameData.gameName(), whiteUser, blackUser));
        return true;
    }

    public void clear() throws Exception {
        gameAccess.clear();
    }
}