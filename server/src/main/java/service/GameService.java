package service;

import dataaccess.*;
import model.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import chess.ChessGame;

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
        if (authAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException();
        }        
        Random rand = new Random();
        int gameID;
        do {
            gameID = rand.nextInt(9999) + 1;
        } while (gameAccess.gameExists(gameID));

        gameAccess.createGame(new GameData(new ChessGame(), gameID, gameName, null, null));

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
        if (gameData == null) {
            throw new BadRequestException("Error: GameID doesn't exist");
        }

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();
        String joiningUser = authData.username();

        if (Objects.equals(color, "WHITE")) {
            if (whiteUser != null) {
                if (joiningUser == whiteUser) {
                    return true;
                }
                return false;
            } else {
                whiteUser = joiningUser;
            }
        } else if (Objects.equals(color, "BLACK")) {
            if (blackUser != null) {
                if (joiningUser == blackUser) {
                    return true;
                }
                return false;
            } else {
                blackUser = joiningUser;
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

    public GameData getGame(String authToken, int gameID) throws Exception {
        AuthData auth = authAccess.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException();
        }
        return gameAccess.getGame(gameID);
    }
}