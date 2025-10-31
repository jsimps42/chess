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

    private AuthData checkAuthorization(String authToken) throws UnauthorizedException {
        try {
            return authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new UnauthorizedException("Invalid or expired authentication token.");
        }
    }

    public HashSet<GameData> listGames(String authToken) throws UnauthorizedException, DataAccessException {
        checkAuthorization(authToken);
        return gameAccess.listGames();
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException, DataAccessException {
        checkAuthorization(authToken);

        Random rand = new Random();
        int gameID;
        do {
            gameID = rand.nextInt(9999) + 1;
        } while (gameAccess.gameExists(gameID));

        gameAccess.createGame(new GameData(null, gameID, gameName, null, null));

        return gameID;
    }

    public boolean joinGame(String authToken, int gameID, String color) throws UnauthorizedException, BadRequestException, DataAccessException {
        AuthData authData = checkAuthorization(authToken);

        GameData gameData;
        try {
            gameData = gameAccess.getGame(gameID);
        } catch (DataAccessException e) {
            e.printStackTrace();
            throw new BadRequestException("Game not found: " + e.getMessage());
        }

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
            throw new BadRequestException(String.format("%s is not a valid team color", color));
        }

        gameAccess.updateGame(new GameData(gameData.game(), gameID, gameData.gameName(), whiteUser, blackUser));
        return true;
    }

    public void clear() throws DataAccessException {
        gameAccess.clear();
    }
}
