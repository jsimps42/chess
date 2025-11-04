package service;

import dataaccess.*;
import dataaccess.BadRequestException;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {
    private final UserAccess userAccess;
    private final AuthAccess authAccess;

    public UserService(UserAccess userAccess, AuthAccess authAccess) {
        this.userAccess = userAccess;
        this.authAccess = authAccess;
    }

    public AuthData register(UserData user) throws Exception {
        if (user == null ||
            user.username() == null || user.username().isEmpty() ||
            user.password() == null || user.password().isEmpty() ||
            user.email() == null || user.email().isEmpty()) {
            throw new BadRequestException("Missing required registration fields");
        }

        try {
            userAccess.addUser(userData);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, userData.username());
        authAccess.addAuth(authData);

        return authData;
    }

    public AuthData loginUser(UserData userData) throws Exception {
        boolean userAuth = false;
        try {
            userAuthenticated = userAccess.authenticateUser(userData.username(), userData.password());
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }

        if (userAuthenticated) {
            String authToken = UUID.randomUUID().toString();
            AuthData authData = new AuthData(authToken, userData.username());
            authAccess.addAuth(authData);
            return authData;
        } else {
            throw new UnauthorizedException();
        }
    }


    public void logoutUser(String authToken) throws Exception {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
        authAccess.deleteAuth(authToken);
    }

    public void clear() throws Exception {
        userAccess.clear();
        authAccess.clear();
    }
}