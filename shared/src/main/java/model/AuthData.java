package model;

import java.util.UUID;

public record AuthData(String authToken, String username) {

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }
}
