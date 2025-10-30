package server.handler;

import service.UserService;
import io.javalin.http.Context;
import model.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.BadRequestException;
import dataaccess.UnauthorizedException;
import dataaccess.ForbiddenException;
import dataaccess.DataAccessException;

public class UserHandler {
    private final UserService userService;
    private static final Gson GSON = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        try {
            UserData user = GSON.fromJson(ctx.body(), UserData.class);
            AuthData auth = userService.register(user);
            ctx.status(200).json(Map.of(
                    "username", auth.username(),
                    "authToken", auth.authToken()
            ));
        } catch (BadRequestException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        } catch (ForbiddenException e) {
            ctx.status(403).json(Map.of("message", "Error: already taken"));
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
    }

    public void login(Context ctx) {
        try {
            UserData user = GSON.fromJson(ctx.body(), UserData.class);
            if (user == null || user.username() == null || user.username().isBlank()
                || user.password() == null || user.password().isBlank()) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            AuthData auth = userService.loginUser(user);
            ctx.status(200).json(Map.of("username", auth.username(), "authToken", auth.authToken()));
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: database error"));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
    }

    public void logout(Context ctx) {
        try {
            String token = ctx.header("authorization");
            if (token == null || token.isBlank()) {
                throw new UnauthorizedException("missing auth");
            }
            userService.logoutUser(token);
            ctx.status(200).json(Map.of());
        } catch (UnauthorizedException e) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
        } catch (DataAccessException e) {
            ctx.status(500).json(Map.of("message", "Error: database error"));
        }
    }
}