package service;

import dataaccess.MemoryUserDAO;
import dataaccess.MemoryAuthDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setup() {
        var userDAO = new MemoryUserDAO();
        var authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerSuccess() throws Exception {
        var user = new UserData("testUser", "password", "email@example.com");
        AuthData auth = userService.register(user);
        assertNotNull(auth);
        assertEquals("testUser", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void registerFailsWhenUsernameTaken() throws Exception {
        var user = new UserData("testUser", "pass", "email");
        userService.register(user);
        Exception ex = assertThrows(Exception.class, () -> userService.register(user));
        assertEquals("already taken", ex.getMessage());
    }

    @Test
    void registerFailsWithBadRequest() {
        var user = new UserData(null, "pass", "email");
        Exception ex = assertThrows(Exception.class, () -> userService.register(user));
        assertEquals("bad request", ex.getMessage());
    }
}