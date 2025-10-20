package server;

import io.javalin.*;
import handlers.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        javalin.post("/user", UserHandler::registry);
        javalin.post("/session", SessionHandler::login);
        javalin.delete("/session", SessionHanderl::logout);
        javalin.post("/game", GameHandler::createGame);
        javalin.get("/game", GameHandler::listGames);
        javalin.put("/game", GameHanders::joinGame);
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
