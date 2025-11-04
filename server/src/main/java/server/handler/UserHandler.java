package server.handler;

import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import model.AuthData;
import model.UserData;
import service.UserService;

import java.util.Map;

public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) throws Exception {
        try {
            UserData userData = new Gson().fromJson(ctx.body(), UserData.class);

        if (userData.username() == null || userData.password() == null) {
            throw new BadRequestException("Missing username/password");
        }

        try {
            AuthData authData = userService.createUser(userData);
            ctx.status(HttpStatus.OK).json(authData);
        } catch (BadRequestException e) {
            ctx.status(HttpStatus.FORBIDDEN)
                    .json(Map.of("message", "Error: username already in use"));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void login(Context ctx) throws Exception {
        try {
            UserData userData = new Gson().fromJson(ctx.body(), UserData.class);

        if (userData == null || userData.username() == null || userData.password() == null) {
            throw new BadRequestException("Missing username/password");
        }

            AuthData authData = userService.loginUser(userData);
            ctx.status(200).json(authData);
        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Error: Unauthorized"));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(new ErrorResponse("Malformed JSON"));
        } catch (Exception e) {
            ctx.status(500).json(new ErrorResponse("Internal server error"));
        }
    }

    public void logout(Context ctx) throws Exception {
        try {
            String authToken = ctx.header("authorization");
            userService.logoutUser(authToken);

        ctx.status(HttpStatus.OK).json(Map.of());
    }
}