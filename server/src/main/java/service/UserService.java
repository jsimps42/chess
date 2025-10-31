package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

public class UserService {
    UserAccess userAccess;
    AuthAccess authAccess;

    public UserService(UserAccess userAccess, AuthAccess authAccess) {
        this.userAccess = userAccess;
        this.authAccess = authAccess;
    }

    public AuthData register(UserData user) throws BadRequestException, ForbiddenException, DataAccessException {
        if (user == null ||
            user.username() == null || user.username().isEmpty() ||
            user.password() == null || user.password().isEmpty() ||
            user.email() == null || user.email().isEmpty()) {
            throw new BadRequestException("Missing required registration fields");
        }

        try {
            userAccess.addUser(user);
        } catch (DataAccessException e) {
            e.printStackTrace();

            if (e.getMessage().contains("already exists")) {
                throw new ForbiddenException("User already registered");
            }
            
            throw new DataAccessException("Database error: " + e.getMessage(), e);
        }

        String authToken = AuthData.generateToken();
        AuthData authData = new AuthData(authToken, user.username());
        authAccess.addAuth(authData);

        return authData;
    }

    public AuthData loginUser(UserData userData) throws UnauthorizedException, DataAccessException {
        userAccess.authenticateUser(userData.username(), userData.password());

        String authToken = AuthData.generateToken();
        AuthData authData = new AuthData(authToken, userData.username());
        authAccess.addAuth(authData);

        return authData;
    }

    public void logoutUser(String authToken) throws UnauthorizedException, DataAccessException {
        authAccess.getAuth(authToken);
        authAccess.deleteAuth(authToken);
    }

    public void clear() throws DataAccessException {
        userAccess.clear();
        authAccess.clear();
    }
}