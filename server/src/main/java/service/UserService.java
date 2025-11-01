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

    public AuthData register(UserData user)
            throws BadRequestException, ForbiddenException, DataAccessException {
        if (user == null || user.username() == null || user.username().isBlank()
                || user.password() == null || user.password().isBlank()
                || user.email() == null || user.email().isBlank()) {
            throw new BadRequestException("bad request");
        }
        if (userAccess.getUser(user.username()) != null) {
            throw new ForbiddenException("already taken");
        }
        userAccess.addUser(user);
        String token = AuthData.generateToken();
        AuthData auth = new AuthData(token, user.username());
        authAccess.addAuth(auth);
        return auth;
    }

    public AuthData loginUser(UserData userData) throws DataAccessException {
        userAccess.authenticateUser(userData.username(), userData.password());
        String token = AuthData.generateToken();
        AuthData auth = new AuthData(token, userData.username());
        authAccess.addAuth(auth);
        return auth;
    }

    public void logoutUser(String authToken)
            throws UnauthorizedException, DataAccessException {
        if (authAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException("bad auth");
        }
        authAccess.deleteAuth(authToken);
    }

    public void clear() throws DataAccessException {
        userAccess.clear();
        authAccess.clear();
    }
}