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

    public AuthData register(UserData user) throws Exception {
        if (user == null || user.username() == null ||
          user.password() == null || user.email() == null) {
            throw new BadRequestException("Error: Missing required registration fields");
        }
        if (userAccess.getUser(user.username()) != null) {
                throw new ForbiddenException("Error: User already registered");
        }
        userAccess.addUser(user);
        String authToken = AuthData.generateToken();
        AuthData authData = new AuthData(authToken, user.username());
        authAccess.addAuth(authData);

        return authData;
    }

    public AuthData loginUser(UserData userData) throws Exception {
        if (userData.username() == null || userData.password() == null) {
            throw new BadRequestException("Error: bad request");
        }
        
        boolean userAuth = false;
        userAuth = userAccess.authenticateUser(userData.username(), userData.password());

        if (userAuth) {
            String authToken = AuthData.generateToken();
            AuthData authData = new AuthData(authToken,userData.username());
            authAccess.addAuth(authData);
            return authData;
        } else {
            throw new UnauthorizedException();
        }
    }


    public void logoutUser(String authToken) throws Exception {
        if (authAccess.getAuth(authToken) == null) {
            throw new UnauthorizedException();
        }
        authAccess.deleteAuth(authToken);
    }

    public void clear() throws Exception {
        userAccess.clear();
        authAccess.clear();
    }
}