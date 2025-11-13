package server;

import dataaccess.*;
import io.javalin.Javalin;
import service.*;
import io.javalin.json.JavalinGson;
import server.handler.*;

public class Server {
    UserAccess userAccess;
    AuthAccess authAccess;
    GameAccess gameAccess;
    UserService userService;
    GameService gameService;
    UserHandler userHandler;
    GameHandler gameHandler;
    ClearHandler clearHandler;

    private Javalin server;

    public Server() {
            userAccess = new MemoryUserAccess();
            authAccess = new MemoryAuthAccess();
            gameAccess = new MemoryGameAccess();
        try {
            userAccess = new MySQLUserAccess();
            authAccess = new MySQLAuthAccess();
            gameAccess = new MySQLGameAccess();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        userService = new UserService(userAccess, authAccess);
        gameService = new GameService(gameAccess, authAccess);
        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);
        clearHandler = new ClearHandler(gameService, userService);

        server = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });
        server.post("/user", userHandler::register);
        server.post("/session", userHandler::login);
        server.delete("/session", userHandler::logout);
        server.delete("/db", clearHandler::clear);
        server.post("/game", gameHandler::createGame);
        server.get("/game", gameHandler::listGames);
        server.get("/game/{gameID}", gameHandler::observeGame);
        server.put("/game", gameHandler::joinGame);

        server.exception(BadRequestException.class, (e, ctx) -> ctx.status(400).json(new ErrorResponse("Error: bad request")));
        server.exception(UnauthorizedException.class, (e, ctx) -> ctx.status(401).json(new ErrorResponse("Error: unauthorized")));
        server.exception(ForbiddenException.class, (e, ctx) -> ctx.status(403).json(new ErrorResponse("Error: forbidden")));
        server.exception(DataAccessException.class, (e, ctx) -> ctx.status(500).json(new ErrorResponse("Internal Server Error")));
        server.exception(Exception.class, (e, ctx) -> ctx.status(500).json(new ErrorResponse("Internal Server Error")));
    }

    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private static class ErrorResponse {
        public final String message;
        public ErrorResponse(String message) { 
            this.message = message; 
        }
    }
}