package server.handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.UserService;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context context) {
        try {
            UserData request = gson.fromJson(context.body(), UserData.class);
            AuthData auth = userService.register(request);
            context.status(200).json(auth);
        } catch (Exception e) {
            String msg = e.getMessage();
            int status = switch (msg) {
                case "bad request" -> 400;
                case "already taken" -> 403;
                default -> 500;
            };
            context.status(status).json(new ErrorResponse("Error: " + msg));
        }
    }

    record ErrorResponse(String message) {}
}
