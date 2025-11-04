package server;

import dataaccess.*;
import server.handler.*;
import io.javalin.*;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinGson;
import org.eclipse.jetty.websocket.api.Session;
import service.GameService;
import service.UserService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final Javalin javalin;

    static UserService userService;
    static GameService gameService;

    private final UserHandler userHandler;
    private final GameHandler gameHandler;

    static ConcurrentHashMap<Session, Integer> gameSessions = new ConcurrentHashMap<>();

    public Server() {
        try {
            userAccess = new MySQLUserAccess();
            authAccess = new MySQLAuthAccess();
            gameAccess = new MySQLGameAccess();
        } catch (Exception e) {
            userAccess = new MemoryUserAccess();
            authAccess = new MemoryAuthAccess();
            gameAccess = new MemoryGameAccess();
        }
        userService = new UserService(userAccess, authAccess);
        gameService = new GameService(gameAccess, authAccess);
        userHandler = new UserHandler(userService);
        gameHandler = new GameHandler(gameService);
    }

    public int run(int desiredPort) {
        server = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        }).start(desiredPort);

        server.post("/user", userHandler::register);
        server.post("/session", userHandler::login);
        server.delete("/session", userHandler::logout);
        server.delete("/db", this::clear);
        server.post("/game", gameHandler::createGame);
        server.get("/game", gameHandler::listGames);
        server.put("/game", gameHandler::joinGame);

        server.exception(BadRequestException.class, (e, ctx) -> ctx.status(400).json(new ErrorResponse("Error: bad request")));
        server.exception(UnauthorizedException.class, (e, ctx) -> ctx.status(401).json(new ErrorResponse("Error: unauthorized")));
        server.exception(Exception.class, (e, ctx) -> ctx.status(500).json(new ErrorResponse("Internal server error")));

        return server.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void clear(Context ctx) throws Exception {
        gameService.clear();
        userService.clear();
        ctx.status(200).json("{}");
    }

    private static class ErrorResponse {
        public final String message;
        public ErrorResponse(String message) { 
            this.message = message; 
        }
    }
}