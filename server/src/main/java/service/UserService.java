package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData register(UserData user) throws Exception {
        if (user.username() == null || user.username().isBlank() ||
            user.password() == null || user.password().isBlank() ||
            user.email() == null || user.email().isBlank()) {
            throw new Exception("bad request");
        }

        if (userDAO.getUser(user.username()) != null) {
            throw new Exception("already taken");
        }

        userDAO.addUser(user);

        String token = AuthData.generateToken();
        AuthData auth = new AuthData(token, user.username());
        authDAO.addAuth(auth);
        return auth;
    }
}
