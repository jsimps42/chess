package server.handler;

import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void clear(Context context) {
        try {
            clearService.clear();
            context.status(200);
        } catch (Exception e) {
            context.status(500).json(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
}
