package server.handler;

import service.GameService;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import java.util.HashSet;
import java.util.Map;
import model.GameData;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import dataaccess.DataAccessException;

public class GameHandler {

    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    // HELPER: Extract auth token robustly
    private String getAuthToken(Context ctx) {
        String token = ctx.header("authorization");
        if (token == null) token = ctx.header("Authorization");
        if (token == null) token = ctx.header("AUTHORIZATION");
        
        if (token != null) {
            token = token.trim();
            if (token.toLowerCase().startsWith("bearer ")) {
                token = token.substring(7).trim();
            }
        }
        return token;
    }

    public void listGames(Context ctx) throws DataAccessException {
        try {
            String authToken = getAuthToken(ctx);
            System.out.println("[HANDLER] listGames() called with auth header: " + ctx.headerMap());
            System.out.println("[HANDLER] extracted token = " + authToken);
            if (authToken == null || authToken.isBlank()) {
                throw new UnauthorizedException("missing auth token");
            }

            HashSet<GameData> games = gameService.listGames(authToken);
            ctx.status(200).json(Map.of("games", games));

        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (DataAccessException e) {
            throw e;
        }
    }

    public void createGame(Context ctx) throws DataAccessException {
        try {
            String authToken = getAuthToken(ctx);
            if (authToken == null || authToken.isBlank()) {
                throw new UnauthorizedException("missing auth token");
            }

            CreateGameRequest req = new Gson().fromJson(ctx.body(), CreateGameRequest.class);
            if (req.gameName() == null || req.gameName().trim().isEmpty()) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            int gameID = gameService.createGame(authToken, req.gameName());
            ctx.status(200).json(Map.of("gameID", gameID));

        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            throw e;
        }
    }

    public void joinGame(Context ctx) throws DataAccessException {
        try {
            String authToken = getAuthToken(ctx);
            if (authToken == null || authToken.isBlank()) {
                throw new UnauthorizedException("missing auth token");
            }

            if (!ctx.body().contains("\"gameID\":")) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            JoinGameData joinData = new Gson().fromJson(ctx.body(), JoinGameData.class);
            boolean success = gameService.joinGame(authToken, joinData.gameID(), joinData.playerColor());
            if (!success) {
                ctx.status(403).json(Map.of("message", "Error: already taken"));
                return;
            }

            ctx.status(200).json(Map.of());

        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (BadRequestException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            throw e;
        }
    }

    public static record CreateGameRequest(String gameName) {}
    public static record JoinGameData(String playerColor, int gameID) {}
}