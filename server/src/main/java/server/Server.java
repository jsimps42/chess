package server;

import io.javalin.Javalin;
import io.javalin.http.Context;
import com.google.gson.Gson;

import dataaccess.*;
import model.*;
import service.*;

public class Server {
    private final Javalin app;
    private final Gson gson = new Gson();

    private final UserService userService;
    private final SessionService sessionService;
    private final GameService gameService;
    private final ClearService clearService;
    private final AuthDAO authDAO;

    public Server() {
        var userDAO = new MemoryUserDAO();
        var gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();

        userService = new UserService(userDAO, authDAO);
        sessionService = new SessionService(userDAO, authDAO);
        gameService = new GameService(gameDAO);
        clearService = new ClearService(userDAO, authDAO, gameDAO);

        app = Javalin.create(config -> config.staticFiles.add("web"));

        registerRoutes();
    }

    public int run(int desiredPort) {
        app.start(desiredPort);
        return app.port();
    }

    public void stop() {
        app.stop();
    }

    private void registerRoutes() {
        // --- CLEAR ---
        app.delete("/db", ctx -> {
            clearService.clear();
            ctx.status(200);
        });

        // --- REGISTER USER ---
        app.post("/user", ctx -> {
            try {
                var user = gson.fromJson(ctx.body(), UserData.class);
                var auth = userService.register(user);
                ctx.status(200).json(auth);
            } catch (Exception e) {
                sendError(ctx, e.getMessage());
            }
        });

        // --- LOGIN ---
        app.post("/session", ctx -> {
            try {
                var req = gson.fromJson(ctx.body(), UserData.class);
                var auth = sessionService.login(req);
                ctx.status(200).json(auth);
            } catch (Exception e) {
                sendError(ctx, e.getMessage());
            }
        });

        // --- LOGOUT ---
        app.delete("/session", ctx -> {
            var token = extractAuth(ctx);
            if (token == null || authDAO.getAuth(token) == null) {
                ctx.status(401).json(new ErrorResponse("Error: unauthorized"));
                return;
            }
            authDAO.removeAuth(token);
            ctx.status(200);
        });

        // --- CREATE GAME ---
        app.post("/game", ctx -> {
            var token = extractAuth(ctx);
            if (token == null || authDAO.getAuth(token) == null) {
                ctx.status(401).json(new ErrorResponse("Error: unauthorized"));
                return;
            }

            var req = gson.fromJson(ctx.body(), CreateGameRequest.class);
            if (req == null || req.gameName() == null || req.gameName().isBlank()) {
                ctx.status(400).json(new ErrorResponse("Error: bad request"));
                return;
            }

            var game = gameService.createGame(req.gameName());
            if (game == null) {
                ctx.status(500).json(new ErrorResponse("Error: could not create game"));
                return;
        }

            ctx.status(200).json(new CreateGameResponse(game.gameID()));
        });


        // --- LIST GAMES ---
        app.get("/game", ctx -> {
            var token = extractAuth(ctx);
            if (token == null || authDAO.getAuth(token) == null) {
                ctx.status(401).json(new ErrorResponse("Error: unauthorized"));
                return;
            }

            var games = gameService.listGames().toArray(new GameData[0]);
            ctx.status(200).json(new ListGamesResponse(games));
        });

        // --- JOIN GAME ---
        app.put("/game", ctx -> {
            var token = extractAuth(ctx);
            var auth = authDAO.getAuth(token);
            if (token == null || auth == null) {
                ctx.status(401).json(new ErrorResponse("Error: unauthorized"));
                return;
            }

            var req = gson.fromJson(ctx.body(), JoinGameRequest.class);
            if (req == null || req.gameID() == null || req.playerColor() == null ||
                (!req.playerColor().equals("WHITE") && !req.playerColor().equals("BLACK"))) {
                ctx.status(400).json(new ErrorResponse("Error: bad request"));
                return;
            }

            var game = gameService.getGame(req.gameID());
            if (game == null) {
                ctx.status(400).json(new ErrorResponse("Error: bad request"));
                return;
            }

            var user = auth.username();
            var updated = handleJoin(game, req.playerColor(), user);
            if (updated == null) {
                ctx.status(403).json(new ErrorResponse("Error: forbidden"));
                return;
            }

            gameService.updateGame(updated);
            ctx.status(200);
        });
    }

    private GameData handleJoin(GameData game, String color, String username) {
        if (color.equals("WHITE")) {
            if (game.whiteUsername() == null) {
                return new GameData(game.game(), game.gameID(), game.gameName(), username, game.blackUsername());
            }
        } else if (color.equals("BLACK")) {
            if (game.blackUsername() == null) {
                return new GameData(game.game(), game.gameID(), game.gameName(), game.whiteUsername(), username);
            }
        }
        return null;
    }

    private void sendError(Context ctx, String message) {
        switch (message) {
            case "bad request" -> ctx.status(400).json(new ErrorResponse("Error: bad request"));
            case "already taken" -> ctx.status(403).json(new ErrorResponse("Error: forbidden"));
            case "invalid credentials" -> ctx.status(401).json(new ErrorResponse("Error: unauthorized"));
            default -> ctx.status(500).json(new ErrorResponse("Error: " + message));
        }
    }

    private String extractAuth(Context ctx) {
        var header = ctx.header("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }

    // DTOs expected by tests
    record ErrorResponse(String message) {}
    record CreateGameRequest(String gameName) {}
    record JoinGameRequest(String playerColor, Integer gameID) {}
    record CreateGameResponse(int gameID) {}
    record ListGamesResponse(GameData[] games) {}
}
