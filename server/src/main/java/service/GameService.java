package service;

import chess.ChessBoard;
import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GameService {
    private final GameAccess gameAccess;
    private final AuthAccess authAccess;

    public GameService(GameAccess gameAccess, AuthAccess authAccess) {
        this.gameAccess = gameAccess;
        this.authAccess = authAccess;
    }

    public HashSet<GameData> listGames(String authToken) throws UnauthorizedException, DataAccessException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException("Data access error: " + e.getMessage());
        }
        return gameAccess.listGames();
    }

    public GameData getGameData(String authToken, int gameID) throws UnauthorizedException, BadRequestException, DataAccessException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }

        try {
            return gameAccess.getGame(gameID);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void updateGame(String authToken, GameData gameData) throws UnauthorizedException, BadRequestException, DataAccessException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }

        if (gameData == null) {
            throw new BadRequestException("No game data");
        }

        try {
            gameAccess.getGame(gameData.gameID());
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }

        try {
            gameAccess.updateGame(gameData);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException, BadRequestException, DataAccessException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }

        Random random = ThreadLocalRandom.current();
        int gameID = -1;
        for (int attempts = 0; attempts < 10000; attempts++) {
            int candidate = random.nextInt(1, 10000);
            if (!gameAccess.gameExists(candidate)) {
                gameID = candidate;
                break;
            }
        }

        if (gameID == -1) {
            throw new BadRequestException("No game IDs left.");
        }

        try {
            ChessGame game = new ChessGame();
            ChessBoard board = new ChessBoard();
            board.resetBoard();
            game.setBoard(board);
            gameAccess.createGame(new GameData(game, gameID, gameName, null, null));
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }

        return gameID;
    }

    public void joinGame(String authToken, int gameID, String color)
            throws UnauthorizedException, BadRequestException, ForbiddenException, DataAccessException {
        AuthData authData;
        GameData gameData;

        try {
            authData = authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }

        try {
            gameData = gameAccess.getGame(gameID);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }

        String whiteUser = gameData.whiteUsername();
        String blackUser = gameData.blackUsername();

        if (Objects.equals(color, "WHITE")) {
            if (whiteUser != null && !whiteUser.equals(authData.username())) {
                throw new ForbiddenException("Color already taken");
            } else {
                whiteUser = authData.username();
            }
        } else if (Objects.equals(color, "BLACK")) {
            if (blackUser != null && !blackUser.equals(authData.username())) {
                throw new ForbiddenException("Color already taken");
            } else {
                blackUser = authData.username();
            }
        } else if (color != null) {
            throw new BadRequestException("Invalid team color: " + color);
        }

        try {
            gameAccess.updateGame(new GameData(gameData.game(), gameID, gameData.gameName(), whiteUser, blackUser));
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void clear() throws DataAccessException {
        gameAccess.clear();
    }
}