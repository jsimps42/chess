package server;

import io.javalin.Javalin;
import server.handler.*;
import dataaccess.*;
import service.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        UserDAO userDAO = new MemoryUserDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        GameDAO gameDAO = new MemoryGameDAO();

        UserService userService = new UserService(userDAO, authDAO);
        SessionService sessionService = new SessionService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO);
        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);

        UserHandler userHandler = new UserHandler(userService);
        SessionHandler sessionHandler = new SessionHandler(sessionService);
        GameHandler gameHandler = new GameHandler(gameService);
        ClearHandler clearHandler = new ClearHandler(clearService);

        javalin.post("/user", userHandler::register);
        javalin.post("/session", sessionHandler::login);
        javalin.delete("/session", sessionHandler::logout);
        javalin.post("/game", gameHandler::createGame);
        javalin.get("/game", gameHandler::listGames);
        javalin.put("/game", gameHandler::joinGame);
        javalin.delete("/db", clearHandler::clear);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
