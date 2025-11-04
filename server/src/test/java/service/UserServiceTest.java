package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserAccess userAccess;
    private AuthAccess authAccess;
    private UserService userService;

    @BeforeEach
    void setup() {
        userAccess = new MemoryUserAccess();
        authAccess = new MemoryAuthAccess();
        userService = new UserService(userAccess, authAccess);
    }

    @Test
    void createUserSuccess() throws Exception {
        UserData user = new UserData("John", "password", "John@yahoo.com");
        AuthData auth = userService.createUser(user);
        assertNotNull(auth);
        assertEquals("John", auth.username());
        assertEquals("John", authAccess.getAuth(auth.authToken()).username());
        assertTrue(userAccess.authenticateUser("John", "password"));
    }

    @Test
    void createUserDuplicate() throws Exception {
        UserData user = new UserData("John", "password", "John@yahoo.com");
        userService.createUser(user);
        assertThrows(DataAccessException.class, () -> userService.createUser(user));
    }

    @Test
    void loginUserSuccess() throws Exception {
        UserData user = new UserData("John", "password", "John@yahoo.com");
        userService.createUser(user);
        AuthData auth = userService.loginUser(new UserData("John", "password", null));
        assertNotNull(auth);
        assertEquals("John", auth.username());
        assertEquals("John", authAccess.getAuth(auth.authToken()).username());
    }

    @Test
    void loginUserWrongPassword() throws Exception {
        UserData user = new UserData("John", "password", "John@yahoo.com");
        userService.createUser(user);
        assertThrows(UnauthorizedException.class,
          () -> userService.loginUser(new UserData("John", "no", null)));
    }

    @Test
    void logoutUserSuccess() throws Exception {
        UserData user = new UserData("John", "password", "John@yahoo.com");
        AuthData auth = userService.createUser(user);
        userService.logoutUser(auth.authToken());
        assertThrows(DataAccessException.class, () -> userService.getAuth(auth.authToken()));
    }

    @Test
    void logoutUserInvalidToken() {
        assertThrows(UnauthorizedException.class, () -> userService.logoutUser("no"));
    }

    @Test
    void getAuthSuccess() throws Exception {
        UserData user = new UserData("John", "password", "John@yahoo.com");
        AuthData auth = userService.createUser(user);
        AuthData lookUp = userService.getAuth(auth.authToken());
        assertEquals(auth.username(), lookUp.username());
    }

    @Test
    void getAuthInvalid() {
        assertThrows(DataAccessException.class, () -> userService.getAuth("missing"));
    }

    @Test
    void clearResetsUsersAndAuth() throws Exception {
        UserData user = new UserData("John", "password", "John@yahoo.com");
        AuthData auth = userService.createUser(user);
        userService.clear();

        assertThrows(DataAccessException.class, () -> userService.getAuth(auth.authToken()));
        assertThrows(DataAccessException.class, () -> userAccess.authenticateUser("John", "password"));
    }
}