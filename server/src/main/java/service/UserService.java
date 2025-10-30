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
        if (user == null ||
            user.username() == null || user.username().isEmpty() ||
            user.password() == null || user.password().isEmpty() ||
            user.email() == null || user.email().isEmpty()) {
            throw new BadRequestException("Missing required registration fields");
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

    public AuthData loginUser(UserData userData) throws UnauthorizedException, DataAccessException {
        boolean userAuth = false;
        try {
            userAuth = userAccess.authenticateUser(userData.username(), userData.password());
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }

        if (userAuth) {
            String authToken = AuthData.generateToken();
            AuthData authData = new AuthData(authToken,userData.username());
            authAccess.addAuth(authData);
            return authData;
        } else {
            throw new UnauthorizedException();
        }
    }


    public void logoutUser(String authToken) throws UnauthorizedException, DataAccessException{
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
        authAccess.deleteAuth(authToken);
    }

    public void clear() throws DataAccessException{
        userAccess.clear();
        authAccess.clear();
    }
}