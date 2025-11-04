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

    public Server() throws DataAccessException{
        try {
            UserAccess userAccess = new MySQLUserAccess();
            AuthAccess authAccess = new MySQLAuthAccess();
            GameAccess gameAccess = new MySQLGameAccess();

            userService = new UserService(userAccess, authAccess);
            gameService = new GameService(gameAccess, authAccess);

            userHandler = new UserHandler(userService);
            gameHandler = new GameHandler(gameService);

            DatabaseManager.createDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

        javalin = Javalin.create(config -> {
            config.staticFiles.add("web");
            config.jsonMapper(new JavalinGson());
        });

        javalin.delete("/db", this::clear);

        javalin.post("/user", userHandler::register);
        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);

        javalin.get("/game", gameHandler::listGames);
        javalin.post("/game", gameHandler::createGame);
        javalin.put("/game", gameHandler::joinGame);

        javalin.exception(BadRequestException.class, (e, ctx) -> {
            ctx.status(HttpStatus.BAD_REQUEST).result("{ \"message\": \"Error: bad request\" }");
        });
        javalin.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.status(HttpStatus.UNAUTHORIZED).result("{ \"message\": \"Error: unauthorized\" }");
        });
        javalin.exception(ForbiddenException.class, (e, ctx) -> {
            ctx.status(HttpStatus.FORBIDDEN).result("{ \"message\": \"Error: forbidden\" }");
        });
        javalin.exception(DataAccessException.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("{ \"message\": \"Error: data access\" }");
        });
        javalin.exception(Exception.class, (e, ctx) -> {
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).result("{ \"message\": \"Error: %s\" }".formatted(e.getMessage()));
        });
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private void clear(Context ctx) {
        try {
            userService.clear();
            gameService.clear();

            ctx.status(HttpStatus.OK).json(Map.of());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}