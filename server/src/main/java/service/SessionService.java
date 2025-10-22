package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

public class SessionService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public SessionService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public AuthData login(UserData user) throws Exception {
        if (user.username() == null || user.password() == null) {
            throw new Exception("bad request");
        }

        UserData storedUser = userDAO.getUser(user.username());

        if (storedUser == null || !storedUser.password().equals(user.password())) {
            throw new Exception("invalid credentials");
        }

        String token = AuthData.generateToken();
        AuthData auth = new AuthData(token, user.username());
        authDAO.addAuth(auth);
        return auth;
    }

    public void logout(String authToken) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            throw new Exception("bad request");
        }

        authDAO.removeAuth(authToken);
    }
}
