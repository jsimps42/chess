package server.handler;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import dataaccess.ForbiddenException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import model.GameData;
import service.GameService;
import java.util.HashSet;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void listGames(Context ctx) throws UnauthorizedException, DataAccessException {
        String authToken = ctx.header("authorization");

        record GameSummary(Integer gameID, String gameName, String whiteUsername, String blackUsername) {}
        HashSet<GameSummary> games = gameService.listGames(authToken)
                .stream()
                .map(g -> new GameSummary(g.gameID(), g.gameName(), g.whiteUsername(), g.blackUsername()))
                .collect(java.util.stream.Collectors.toCollection(HashSet::new));

        record ListGamesResponse(HashSet<GameSummary> games) {}
        ctx.status(HttpStatus.OK).json(new ListGamesResponse(games));
    }

    public void createGame(Context ctx) throws BadRequestException, UnauthorizedException, DataAccessException {
        if (!ctx.body().contains("\"gameName\":")) {
            throw new BadRequestException("Missing gameName");
        }

        GameData gameData = ctx.bodyAsClass(GameData.class);

        String authToken = ctx.header("authorization");
        int gameID = gameService.createGame(authToken, gameData.gameName());

        record CreateGameResponse(Integer gameID) {}
        ctx.status(HttpStatus.OK).json(new CreateGameResponse(gameID));
    }

    public void joinGame(Context ctx) throws BadRequestException, UnauthorizedException, DataAccessException, ForbiddenException {
        if (!ctx.body().contains("\"gameID\":")) {
            throw new BadRequestException("Missing gameID");
        }

        record JoinGameRequest(String playerColor, Integer gameID) {}
        String authToken = ctx.header("authorization");
        JoinGameRequest req = ctx.bodyAsClass(JoinGameRequest.class);

        String color = req == null
                ? null
                : req.playerColor;
        Integer gameID = req == null
                ? null
                : req.gameID;

        if (gameID == null) {
            throw new BadRequestException("Missing gameID");
        }

        if (color == null || color.isBlank() || !(color.equals("WHITE") || color.equals("BLACK"))) {
            throw new BadRequestException("Invalid color");
        }

        record JoinGameData(String playerColor, int gameID) {}
        JoinGameData joinData = ctx.bodyAsClass(JoinGameData.class);

        gameService.joinGame(authToken, joinData.gameID, joinData.playerColor);
        ctx.status(HttpStatus.OK).json(Map.of());
    }
}