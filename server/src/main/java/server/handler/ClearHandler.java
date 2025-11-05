package server.handler;

import java.util.Map;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import service.GameService;
import service.UserService;

public class ClearHandler {

    private final GameService gameService;
    private final UserService userService;

    public ClearHandler(GameService gameService, UserService userService) {
        this.gameService = gameService;
        this.userService = userService;
    }

    public void clear(Context ctx) throws Exception {
        try {
            this.gameService.clear();
            this.userService.clear();
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        } catch (Exception e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
    }
}