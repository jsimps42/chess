package service;

import dataaccess.*;
import model.*;

public class UserService {
    private final UserAccess userAccess;
    private final AuthAccess authAccess;

    public UserService(UserAccess userAccess, AuthAccess authAccess) {
        this.userAccess = userAccess;
        this.authAccess = authAccess;
    }

    public AuthData register(UserData user) throws BadRequestException, ForbiddenException, DataAccessException {
        if (user == null || user.username() == null || user.username().isEmpty() ||
            user.password() == null || user.password().isEmpty() ||
            user.email() == null || user.email().isEmpty()) {
            throw new BadRequestException("Missing required fields");
        }

        if (userAccess.getUser(user.username()) != null) {
            throw new ForbiddenException("User already exists");
        }

        userAccess.addUser(user);
        String token = AuthData.generateToken();
        AuthData auth = new AuthData(token, user.username());
        authAccess.addAuth(auth);
        return auth;
    }

    public AuthData loginUser(UserData userData) throws UnauthorizedException, DataAccessException {
        try {
            userAccess.authenticateUser(userData.username(), userData.password());
        } catch (DataAccessException e) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = AuthData.generateToken();
        AuthData auth = new AuthData(token, userData.username());
        authAccess.addAuth(auth);
        return auth;
    }

    public void logoutUser(String authToken) throws UnauthorizedException, DataAccessException {
        if (authAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("Invalid auth token");
        }
        authAccess.deleteAuth(authToken);
    }

    public void clear() throws DataAccessException {
        userAccess.clear();
        authAccess.clear();
    }
}