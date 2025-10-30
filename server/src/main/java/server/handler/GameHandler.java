package server.handler;

import com.google.gson.*;
import io.javalin.http.Context;
import model.request.CreateGameRequest;
import service.*;
import chess.ChessGame;
import java.util.Map;
import java.util.Set;

import dataaccess.*;
import model.GameData;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    private String getAuthToken(Context ctx) {
        String token = ctx.header("authorization");
        if (token == null) {
            token = ctx.header("Authorization");
        }
        if (token != null && token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }
        return token;
    }

    public void createGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (authToken == null || authToken.isBlank()) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            CreateGameRequest req = gson.fromJson(ctx.body(), CreateGameRequest.class);
            if (req == null || req.gameName() == null || req.gameName().isBlank()) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            int gameID = gameService.createGame(req.gameName(), authToken);
            ctx.status(200).json(Map.of("gameID", gameID));

        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (BadRequestException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: database error"));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
    }

    public void joinGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            if (authToken == null || authToken.isBlank()) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            JsonObject body = gson.fromJson(ctx.body(), JsonObject.class);
            if (body == null || !body.has("gameID")) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            int gameID = body.get("gameID").getAsInt();
            ChessGame.TeamColor playerColor = null;
            if (body.has("playerColor")) {
                String color = body.get("playerColor").getAsString();
                playerColor = ChessGame.TeamColor.valueOf(color.toUpperCase());
            }

            gameService.joinGame(authToken, gameID, playerColor);
            ctx.status(200).json(Map.of());

        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (BadRequestException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: database error"));
        } catch (JsonSyntaxException | IllegalArgumentException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
    }    

    public void listGames(Context ctx) {
        String token = getAuthToken(ctx);
        if (token == null || token.isBlank()) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
            return;
        }
        try {
            Set<GameData> games = gameService.listGames(token);
            ctx.status(200).json(Map.of("games", games));
        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: database error"));
        }
    }
}