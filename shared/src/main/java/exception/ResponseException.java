package exception;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ResponseException extends Exception {

    public enum Code { ServerError, ClientError }

    private final Code code;
    private final int httpStatus;

    public ResponseException(Code code, String message, int httpStatus) {
        super(message);
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public ResponseException(Code code, String message) {
        this(code, message, code == Code.ServerError ? 500 : 400);
    }

    public int getStatusCode() { return httpStatus; }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", code));
    }

    public static ResponseException fromJson(String json, int httpStatus) {
        try {
            var map = new Gson().fromJson(json, HashMap.class);
            if (map == null) {
                return new ResponseException(Code.ClientError, "Empty error body", httpStatus);
            }

            Code code = Code.ClientError;
            Object statusObj = map.get("status");
            if (statusObj instanceof String statusStr) {
                try { code = Code.valueOf(statusStr); }
                catch (IllegalArgumentException ignored) {}
            }

            String message = "Server error";
            Object msgObj = map.get("message");
            if (msgObj instanceof String msgStr) {
                message = msgStr;
            } else if (msgObj != null) {
                message = msgObj.toString();
            }

            return new ResponseException(code, message, httpStatus);
        } catch (Exception e) {
            return new ResponseException(Code.ClientError, "Failed to parse error: " + json, httpStatus);
        }
    }

    public static ResponseException fromJson(String json) {
        return fromJson(json, 400);
    }

    public static Code fromHttpStatusCode(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 500 -> Code.ServerError;
            case 400, 401, 403 -> Code.ClientError;
            default -> throw new IllegalArgumentException("Unknown HTTP status code: " + httpStatusCode);
        };
    }
}