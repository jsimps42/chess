package dataaccess;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import dataaccess.*;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

public class MySQLUserAccessTest {

    private static MySQLUserAccess userAccess;

    @BeforeAll
    public static void setup() {
        userAccess = new MySQLUserAccess();
    }

    @BeforeEach
    public void clearBefore() throws DataAccessException {
        userAccess.clear();
    }

    @Test
    @DisplayName("Test Add User - Positive Case")
    public void testAddUser() {
        try {
            UserData newUser = new UserData("testUser", "password123", "testuser@example.com");

            userAccess.addUser(newUser);

            UserData retrievedUser = userAccess.getUser("testUser");
            assertNotNull(retrievedUser, "User should be found in the database.");
            assertEquals("testUser", retrievedUser.username());
            assertEquals("testuser@example.com", retrievedUser.email());
        } catch (DataAccessException e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Add User - Negative Case (Username Already Exists)")
    public void testAddUserWithExistingUsername() {
        try {
            UserData existingUser = new UserData("existingUser", "password123", "existing@example.com");
            userAccess.addUser(existingUser);

            UserData newUser = new UserData("existingUser", "newPassword123", "new@example.com");

            assertThrows(DataAccessException.class, () -> userAccess.addUser(newUser), 
                "Adding a user with the same username should throw a DataAccessException.");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Get User - Positive Case")
    public void testGetUser() {
        try {
            UserData newUser = new UserData("testUser", "password123", "testuser@example.com");
            userAccess.addUser(newUser);

            UserData retrievedUser = userAccess.getUser("testUser");

            assertNotNull(retrievedUser, "User should be found in the database.");
            assertEquals("testUser", retrievedUser.username());
            assertEquals("testuser@example.com", retrievedUser.email());
        } catch (DataAccessException e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Get User - Negative Case (User Does Not Exist)")
    public void testGetNonExistentUser() {
        try {
            UserData retrievedUser = userAccess.getUser("nonExistentUser");

            assertNull(retrievedUser, "User should not be found in the database.");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Authenticate User - Positive Case")
    public void testAuthenticateUser() {
        try {
            UserData newUser = new UserData("testUser", "password123", "testuser@example.com");
            userAccess.addUser(newUser);

            userAccess.authenticateUser("testUser", "password123");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Authenticate User - Negative Case (Incorrect Password)")
    public void testAuthenticateUserIncorrectPassword() {
        try {
            UserData newUser = new UserData("testUser", "password123", "testuser@example.com");
            userAccess.addUser(newUser);

            assertThrows(DataAccessException.class, () -> userAccess.authenticateUser("testUser", "wrongPassword"),
                "Authenticating with an incorrect password should throw a DataAccessException.");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Clear - Positive Case")
    public void testClear() {
        try {
            UserData newUser = new UserData("testUser", "password123", "testuser@example.com");
            userAccess.addUser(newUser);

            userAccess.clear();

            UserData retrievedUser = userAccess.getUser("testUser");
            assertNull(retrievedUser, "User should be deleted from the database.");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            userAccess.clear();
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}