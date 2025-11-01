package model;

import java.util.Objects;
import java.util.UUID;

public record AuthData(String authToken, String username) {
    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AuthData that)) return false;
        return Objects.equals(authToken(), that.authToken())
                && Objects.equals(username(), that.username());
    }

    @Override
    public int hashCode() {
        return Objects.hash(authToken(), username());
    }
}