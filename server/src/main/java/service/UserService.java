package service;

import java.util.UUID;
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

    public AuthData register(UserData user) throws BadRequestException, ForbiddenException {
        if (user == null ||
            user.username() == null || user.username().isEmpty() ||
            user.password() == null || user.password().isEmpty() ||
            user.email() == null || user.email().isEmpty()) {
            throw new BadRequestException("Missing required registration fields");
        }

        try {
            userAccess.addUser(user);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("already exists")) {
                throw new ForbiddenException("User already registered");
            }
            throw new BadRequestException(e.getMessage());
        }
        String authToken = AuthData.generateToken();
        AuthData authData = new AuthData(authToken, user.username());
        authAccess.addAuth(authData);

        return authData;
    }

    public AuthData loginUser(UserData userData) throws UnauthorizedException {
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


    public void logoutUser(String authToken) throws UnauthorizedException {
        try {
            authAccess.getAuth(authToken);
        } catch (DataAccessException e) {
            throw new UnauthorizedException();
        }
        authAccess.deleteAuth(authToken);
    }

    public void clear() {
        userAccess.clear();
        authAccess.clear();
    }
}