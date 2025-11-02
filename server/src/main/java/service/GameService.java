package service;

import dataaccess.*;
import model.*;
import chess.ChessGame;
import java.util.HashSet;
//delete this

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
            throw new UnauthorizedException();
        }
        return auth;
    }

    public int createGame(String authToken, String gameName) throws UnauthorizedException, DataAccessException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
        GameData g = new GameData(new ChessGame(), 0, gameName, null, null);
        return gameAccess.createGame(g);
    }

    public HashSet<GameData> listGames(String authToken)
            throws UnauthorizedException, DataAccessException {
        checkAuth(authToken);
        return new HashSet<>(gameAccess.listGames());
    }

    public boolean joinGame(String authToken, int gameID, String color) throws UnauthorizedException, BadRequestException, DataAccessException {

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
        
        String white = game.whiteUsername();
        String black = game.blackUsername();

        if (playerColor == ChessGame.TeamColor.WHITE) {
            if (white != null) {
                return false;
            }
            white = auth.username();
        } else if (playerColor == ChessGame.TeamColor.BLACK) {
            if (black != null) {
                return false;
            }    
            black = auth.username();
        } else {
            throw new BadRequestException("bad request");
        }

        GameData updated = new GameData(game.game(), game.gameID(),
                                        game.gameName(), white, black);
        gameAccess.updateGame(updated);
        return true;
    }

    public void clear() throws DataAccessException {
    public void clear() throws DataAccessException {
        gameAccess.clear();
        authAccess.clear();
    }
}