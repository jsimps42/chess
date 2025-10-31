package service;

import dataaccess.*;
import model.*;
import chess.ChessGame;
import java.util.HashSet;

public class GameService {
    private final GameAccess gameAccess;
    private final AuthAccess authAccess;

    public GameService(GameAccess gameAccess, AuthAccess authAccess) {
        this.gameAccess = gameAccess;
        this.authAccess = authAccess;
    }

    private AuthData checkAuth(String authToken) throws UnauthorizedException {
        try {
            return authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException("Invalid auth");
        }
    }

    public HashSet<GameData> listGames(String authToken) throws UnauthorizedException, DataAccessException {
        checkAuth(authToken);
        return gameAccess.listGames();
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException, DataAccessException {
        checkAuth(authToken);
        ChessGame chess = new ChessGame();
        GameData game = new GameData(chess, 0, gameName, null, null);
        return gameAccess.createGame(game);  // Returns DB-generated ID
    }

    public boolean joinGame(String authToken, int gameID, String color)
            throws UnauthorizedException, BadRequestException, DataAccessException {
        AuthData auth = checkAuth(authToken);
        GameData game = gameAccess.getGame(gameID);
        if (game == null) throw new BadRequestException("Game not found");

        String white = game.whiteUsername();
        String black = game.blackUsername();

        if ("WHITE".equalsIgnoreCase(color)) {
            if (white != null) return false;
            white = auth.username();
        } else if ("BLACK".equalsIgnoreCase(color)) {
            if (black != null) return false;
            black = auth.username();
        } else {
            throw new BadRequestException("Invalid color");
        }

        gameAccess.updateGame(new GameData(game.game(), game.gameID(), game.gameName(), white, black));
        return true;
    }

    public void clear() throws DataAccessException {
        gameAccess.clear();
    }
}