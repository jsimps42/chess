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

    private AuthData checkAuth(String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth = authAccess.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("bad auth");
        }
        return auth;
    }

    public int createGame(String authToken, String gameName)
            throws UnauthorizedException, BadRequestException, DataAccessException {
        checkAuth(authToken);
        if (gameName == null || gameName.isBlank()) {
            throw new BadRequestException("bad request");
        }
        GameData g = new GameData(new ChessGame(), 0, gameName, null, null);
        return gameAccess.createGame(g);
    }

    public HashSet<GameData> listGames(String authToken)
            throws UnauthorizedException, DataAccessException {
        checkAuth(authToken);
        return new HashSet<>(gameAccess.listGames());
    }

    public boolean joinGame(String authToken, int gameID, ChessGame.TeamColor playerColor)
            throws UnauthorizedException, BadRequestException, DataAccessException {
        authAccess.getAuth(authToken);
        AuthData auth = checkAuth(authToken);
        GameData game = gameAccess.getGame(gameID);
        if (game == null) {
            throw new BadRequestException("Game not found");
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
        gameAccess.clear();
        authAccess.clear();
    }
}