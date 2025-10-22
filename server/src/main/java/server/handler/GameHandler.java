package server.handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import model.GameData;
import service.GameService;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void createGame(Context context) {
        try {
            var request = gson.fromJson(context.body(), CreateGameRequest.class);
            GameData game = gameService.createGame(request.gameName());
            context.status(200).json(game);
        } catch (Exception e) {
            context.status(500).json(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    public void listGames(Context context) {
        try {
            var games = gameService.listGames();
            context.status(200).json(games);
        } catch (Exception e) {
            context.status(500).json(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    public void joinGame(Context context) {
        try {
            int gameID = Integer.parseInt(context.queryParam("gameID"));
            String username = context.queryParam("username");
            GameData updatedGame = gameService.joinGame(gameID, username);
            context.status(200).json(updatedGame);
        } catch (Exception e) {
            context.status(500).json(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    record CreateGameRequest(String gameName) {}
    record ErrorResponse(String message) {}
}
