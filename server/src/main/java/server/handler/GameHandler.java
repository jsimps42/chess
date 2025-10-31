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

    public void listGames(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        try {
            HashSet<GameData> games = gameService.listGames(authToken);
            ctx.status(200).json(Map.of("games", games));
        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: Unauthorized"));
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: Internal server error"));
        }
    }

    public void createGame(Context ctx) {
        String authToken = ctx.header("authorization");
        try {
            CreateGameRequest req = new Gson().fromJson(ctx.body(), CreateGameRequest.class);
            String gameName = req.gameName();

            if (gameName == null || gameName.trim().isEmpty()) {
                ctx.status(400).json(Map.of("message", "Error: gameName is missing or empty"));
                return;
            }

            int gameID = gameService.createGame(authToken, gameName);
            ctx.status(200).json(Map.of("gameID", gameID));
        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: Unauthorized"));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(Map.of("message", "Error: Malformed JSON"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    public void joinGame(Context ctx) throws DataAccessException {
        if (!ctx.body().contains("\"gameID\":")) {
            ctx.status(400).json(Map.of("message", "Error: No gameID provided"));
            return;
        }

        String authToken = ctx.header("authorization");
        try {
            JoinGameData joinData = new Gson().fromJson(ctx.body(), JoinGameData.class);
            int gameID = joinData.gameID();
            String playerColor = joinData.playerColor();

            boolean joinSuccess = gameService.joinGame(authToken, gameID, playerColor);
            if (!joinSuccess) {
                ctx.status(403).json(Map.of("message", "Error: Player color already taken"));
                return;
            }

            ctx.status(200).json(Map.of("message", "Successfully joined the game"));
        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: Unauthorized"));
        } catch (BadRequestException e) {
            ctx.status(400).json(Map.of("message", "Error: Bad Request"));
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    public static record CreateGameRequest(String gameName) {}

    public static record JoinGameData(String playerColor, int gameID) {}
}
