package server;

import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.*;
import com.google.gson.Gson;
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

    private Javalin server;

    public Server() {
        try {
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
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("Unexpected error during server startup: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error during server startup", e);
        }
    }

    public int run(int desiredPort) {
        server = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson()); // <-- Add this line here
        }).start(desiredPort);

        server.post("/user", userHandler::register);
        server.post("/session", userHandler::login);
        server.delete("/session", userHandler::logout);
        server.delete("/db", this::clear);
        
        server.post("/game", gameHandler::createGame);
        server.get("/game", gameHandler::listGames);
        server.put("/game", gameHandler::joinGame);

        server.exception(BadRequestException.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(400).json(new ErrorResponse("Error: bad request"));
        });
        server.exception(UnauthorizedException.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(401).json(new ErrorResponse("Error: unauthorized"));
        });
        server.exception(DataAccessException.class, (e, ctx) -> {
            e.printStackTrace();
            ctx.status(500).json(new ErrorResponse("Database error"));
        });
        server.exception(Exception.class, (e, ctx) -> {
            e.printStackTrace();
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
}