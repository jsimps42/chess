package server;

import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.*;
import io.javalin.json.JavalinGson;
import server.handler.*;
import java.util.Properties;

public class Server {
    UserAccess userAccess;
    AuthAccess authAccess;
    GameAccess gameAccess;
    UserService userService;
    GameService gameService;
    UserHandler userHandler;
    GameHandler gameHandler;

    private Javalin server;

    public Server() {
        try {
            DatabaseManager.loadPropertiesFromResources();

            DatabaseManager.createDatabase();
            DatabaseManager.createTables();

            userAccess = new MySQLUserAccess();
            authAccess = new MySQLAuthAccess();
            gameAccess = new MySQLGameAccess();
            userService = new UserService(userAccess, authAccess);
            gameService = new GameService(gameAccess, authAccess);
            userHandler = new UserHandler(userService);
            gameHandler = new GameHandler(gameService);
        } catch (DataAccessException e) {
            throw new RuntimeException("Database initialization failed", e);
        } catch (Exception e) {
            throw new RuntimeException("Server startup failed", e);
        }
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

        server.exception(BadRequestException.class, (e, ctx) -> {
            ctx.status(400).json(new ErrorResponse("Error: bad request"));
        });
        server.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.status(401).json(new ErrorResponse("Error: unauthorized"));
        });
        server.exception(Exception.class, (e, ctx) -> {
            ctx.status(500).json(new ErrorResponse("Internal server error"));
        });

        return server.port();
    }

    public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    private void clear(Context ctx) throws DataAccessException {
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

    public static void loadProperties(Properties props) {
        DatabaseManager.loadProperties(props);
    }
}