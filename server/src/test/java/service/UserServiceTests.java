package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.*;
import model.*;

public class UserServiceTests {
    private UserService userService;
    UserAccess userAccess;
    AuthAccess authAccess;
    UserData testUser = new UserData("user1", "pass", "email@example.com");

    @BeforeEach
    public void setup() {
        var userAccess = new MemoryUserAccess();
        var authAccess = new MemoryAuthAccess();
        userService = new UserService(userAccess, authAccess);
    }

    @Test
    @DisplayName("Register Success")
    void registerSuccess() throws Exception {
        AuthData response = userService.register(testUser);
        assertNotNull(response);
        assertEquals(testUser.username(), response.username());
        assertNotNull(response.authToken());
    }

    @Test
    @DisplayName("Register Missing Fields")
    void registerMissingFieldsThrowsBadRequest() {
        UserData badUser = new UserData(null, "pass", "email@example.com");
        assertThrows(BadRequestException.class, () -> userService.register(badUser));
    }

    @Test
    @DisplayName("Register Already Taken")
    void registerAlreadyTakenThrowsForbidden() throws Exception {
        userService.register(testUser);
        assertThrows(ForbiddenException.class, () -> userService.register(testUser));
    }

    @Test
    @DisplayName("Login Success")
    void loginUserSuccess() throws Exception {
        userService.register(testUser);
        AuthData response = userService.loginUser(testUser);
        assertNotNull(response);
        assertEquals(testUser.username(), response.username());
        assertNotNull(response.authToken());
    }

    @Test
    @DisplayName("Login Unauthorized (Bad Password)")
    void loginUserUnauthorized() throws Exception {
        userService.register(testUser);
        UserData badLogin = new UserData(testUser.username(), "wrong_password", testUser.email());
        assertThrows(UnauthorizedException.class, () -> userService.loginUser(badLogin));
    }

    @Test
    @DisplayName("Logout Success")
    void logoutUserSuccess() throws Exception {
        AuthData response = userService.register(testUser);
        assertDoesNotThrow(() -> userService.logoutUser(response.authToken()));
    }

    @Test
    @DisplayName("Logout Unauthorized (Bad Token)")
    void logoutUserUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> 
            userService.logoutUser("fake_token"));
    }

    @Test
    @DisplayName("UserService clear clears all users and auths")
    void userServiceClear() throws Exception {
        userService.register(testUser);
        userService.clear();
        assertThrows(UnauthorizedException.class, () -> userService.logoutUser("anytoken"));
    }
}