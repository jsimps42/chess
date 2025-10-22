package server;

import io.javalin.*;
import server.handler.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.post("/user", UserHandler::register);
        javalin.post("/session", SessionHandler::login);
        javalin.delete("/session", SessionHander::logout);
        javalin.post("/game", GameHandler::createGame);
        javalin.get("/game", GameHandler::listGames);
        javalin.put("/game", GameHandler::joinGame);
        javalin.delete("/db", ClearHandler::clear);
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
