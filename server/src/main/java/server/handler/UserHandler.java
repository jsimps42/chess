package server.handler;

import com.google.gson.Gson;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.UserService;

public class UserHandler {

    private static final UserService userService = new UserService(new MemoryUserDAO(), 
        new MemoryAuthDAO());
    private static final Gson gson = new Gson();

    public static void register(Context context) {
        try {
            var registerRequest = gson.fromJson(context.body(), UserData.class);
            AuthData authData = userService.register(registerRequest);
            context.status(200).json(authData);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            int status = switch (errorMessage) {
                case "bad request" -> 400;
                case "already taken" -> 403;
                default -> 500;
            };
            context.status(status).json(new ErrorResponse("Error: " + errorMessage));
        }
    }

    record ErrorResponse(String message) {}
}
