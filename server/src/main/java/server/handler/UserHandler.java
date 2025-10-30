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
    UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        try {
            UserData userData = new Gson().fromJson(ctx.body(), UserData.class);

            if (userData.username() == null || userData.password() == null) {
                ctx.status(400).json(new ErrorResponse("Error: No username or password given"));
                return;
            }

            AuthData authData = userService.register(userData);
            ctx.status(200).json(authData);
        } catch (ForbiddenException e) {
            ctx.status(403).json(new ErrorResponse("Error: already registered"));
        } catch (BadRequestException e) {
            ctx.status(400).json(new ErrorResponse("Error: Bad Request"));
        } catch (JsonSyntaxException e) {
            ctx.status(400).json(new ErrorResponse("Malformed JSON"));
        }
    }

    public void login(Context ctx) {
        try {
            UserData userData = new Gson().fromJson(ctx.body(), UserData.class);

            if (userData.username() == null || userData.password() == null) {
                ctx.status(400).json(new ErrorResponse("Error: No username or password given"));
                return;
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

    public void logout(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            userService.logoutUser(authToken);

            ctx.status(200).json(new Gson().fromJson("{}", Object.class));
        } catch (UnauthorizedException e) {
            ctx.status(401).json(new ErrorResponse("Error: unauthorized"));
        }
    }
    
    private static class ErrorResponse {
        public final String message;
        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}