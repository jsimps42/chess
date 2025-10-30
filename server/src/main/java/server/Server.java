package server;

import dataaccess.*;
import io.javalin.Javalin;
import io.javalin.http.Context;
import service.*;
import io.javalin.json.JavalinGson;
import server.handler.*;
import java.util.Map;
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

    public Server() throws DataAccessException{
        try {
            DatabaseManager.createDatabase();
            DatabaseManager.createTables();
            System.out.println("Database and tables verified/created.");
        } catch (DataAccessException e) {
            System.err.println("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }

        userAccess = new MySQLUserAccess();
        authAccess = new MySQLAuthAccess();
        gameAccess = new MySQLGameAccess();
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
        server.delete("/db", ctx -> {
            System.out.println("CLEAR /db CALLED");
            try {
                System.out.println("Clearing users...");
                userService.clear();
                System.out.println("Users cleared");
                System.out.println("Clearing games...");
                gameService.clear();
                System.out.println("Games cleared");
                DatabaseManager.resetTablesCreated();
                ctx.status(200).json(Map.of());
            } catch (Exception e) {
                System.out.println("EXCEPTION IN /db: " + e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace();
                ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
            }
        });
        server.post("/game", gameHandler::createGame);
        server.get("/game", gameHandler::listGames);
        server.put("/game", gameHandler::joinGame);

        server.exception(BadRequestException.class, (e, ctx) -> {
            ctx.status(400).json(new ErrorResponse("Error: bad request"));
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