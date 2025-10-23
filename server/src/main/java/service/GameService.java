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

    public HashSet<GameData> listGames(String authToken) throws UnauthorizedException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
        return gameAccess.listGames();
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        Random rand = new Random();
        int gameID;
        do {
            gameID = rand.nextInt(9999) + 1;
        } while (gameAccess.gameExists(gameID));

        gameAccess.createGame(new GameData(null, gameID, gameName, null, null));

        return gameID;
    }

    public boolean joinGame(String authToken, int gameID, String color) throws UnauthorizedException, BadRequestException {

        AuthData authData;
        GameData gameData;

        try {
            authData = authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        try {
            gameData = gameAccess.getGame(gameID);
        } catch (DataAccessException e) {
            throw new BadRequestException(e.getMessage());
        }

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();

        if (Objects.equals(color, "WHITE")) {
            if (whiteUser != null) {
                return false; // Spot taken
            } else {
                whiteUser = authData.username();
            }
        } else if (Objects.equals(color, "BLACK")) {
            if (blackUser != null) {
                return false; // Spot taken
            } else {
                blackUser = authData.username();
            }
        } else if (color == null || color.isEmpty()) {
            throw new BadRequestException("Error: Player color cannot be null or empty");
        } else {
            throw new BadRequestException("%s is not a valid team color".formatted(color));
        }
        gameAccess.updateGame(new GameData(gameData.game(), gameID, gameData.gameName(), whiteUser, blackUser));
        return true;
    }

    public void clear() {
        gameAccess.clear();
    }
}