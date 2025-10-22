package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private ClearService clearService;

    @BeforeEach
    void setup() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    void clearEmptiesAllData() throws Exception {
        userDAO.addUser(new UserData("user", "pass", "email"));
        gameDAO.createGame("test game");
        authDAO.addAuth(new AuthData("token", "user"));

        clearService.clear();

        assertTrue(userDAO.getUserMap().isEmpty());
        assertTrue(gameDAO.listGames().isEmpty());
        assertTrue(authDAO.listAuthTokens().isEmpty());
    }
}