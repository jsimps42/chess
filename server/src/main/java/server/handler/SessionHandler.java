package server.handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.SessionService;

public class SessionHandler {
    private final SessionService sessionService;
    private final Gson gson = new Gson();

    public SessionHandler(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void login(Context context) {
        try {
            UserData request = gson.fromJson(context.body(), UserData.class);
            AuthData auth = sessionService.login(request);
            context.status(200).json(auth);
        } catch (Exception e) {
            String msg = e.getMessage();
            int status = switch (msg) {
                case "invalid credentials" -> 403;
                case "bad request" -> 400;
                default -> 500;
            };
            context.status(status).json(new ErrorResponse("Error: " + msg));
        }
    }

    public void logout(Context context) {
        try {
            String authToken = context.header("Authorization");
            if (authToken == null) {
                throw new Exception("bad request");
            }
            sessionService.logout(authToken);
            context.status(200);
        } catch (Exception e) {
            String msg = e.getMessage();
            int status = msg.equals("bad request") ? 400 : 500;
            context.status(status).json(new ErrorResponse("Error: " + msg));
        }
    }

    record ErrorResponse(String message) {}
}
