package service;

import dataaccess.MemoryUserDAO;
import dataaccess.MemoryAuthDAO;
import model.UserData;
import model.AuthData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionServiceTest {

    private SessionService sessionService;

    @BeforeEach
    void setup() throws Exception {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        userDAO.addUser(new UserData("test", "pass", "email"));
        sessionService = new SessionService(userDAO, authDAO);
    }

    @Test
    void loginSuccess() throws Exception {
        AuthData auth = sessionService.login(new UserData("test", "pass", "email"));
        assertNotNull(auth.authToken());
        assertEquals("test", auth.username());
    }

    @Test
    void loginFailsWithBadPassword() {
        Exception ex = assertThrows(Exception.class, () -> sessionService.login(new UserData("test", "wrong", "email")));
        assertEquals("invalid credentials", ex.getMessage());
    }

    @Test
    void loginFailsWithMissingUser() {
        Exception ex = assertThrows(Exception.class, () -> sessionService.login(new UserData("none", "pass", "email")));
        assertEquals("invalid credentials", ex.getMessage());
    }
}