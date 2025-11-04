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

    public AuthData createUser(UserData userData) throws BadRequestException, DataAccessException {
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

    public AuthData loginUser(UserData userData) throws UnauthorizedException, DataAccessException {
        boolean userAuthenticated;
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

    public void logoutUser(String authToken) throws UnauthorizedException, DataAccessException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
        authAccess.deleteAuth(authToken);
    }

    public AuthData getAuth(String authToken) throws UnauthorizedException, DataAccessException {
        try {
            return authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    public void clear() throws DataAccessException {
        userAccess.clear();
        authAccess.clear();
    }
}