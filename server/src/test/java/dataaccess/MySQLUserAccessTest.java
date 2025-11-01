package dataaccess;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import dataaccess.*;
import model.UserData;

public class MySQLUserAccessTest {

    private static MySQLUserAccess userAccess;

    @BeforeAll
    public static void setup() {
        userAccess = new MySQLUserAccess();
    }

    @BeforeEach
    public void clearBefore() throws DataAccessException {
        DatabaseManager.loadPropertiesFromResources();
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
        new MySQLUserAccess().clear();
    }

    private void addLoginLogout(String username, String password) throws DataAccessException {
        UserData user = new UserData(username, password, username + "@example.com");
        userAccess.addUser(user);

        userAccess.authenticateUser(username, password);
    }

    @Test
    @DisplayName("Test Add User - Positive Case")
    public void testAddUser() {
        try {
            UserData newUser = new UserData("testUser", "password123", "testuser@example.com");
            userAccess.addUser(newUser);

            UserData retrieved = userAccess.getUser("testUser");
            assertNotNull(retrieved, "User should be found in the database.");
            assertEquals("testUser", retrieved.username());
            assertEquals("testuser@example.com", retrieved.email());
        } catch (DataAccessException e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Add User - Negative Case (Username Already Exists)")
    public void testAddUserWithExistingUsername() {
        try {
            UserData existing = new UserData("existingUser", "password123", "existing@example.com");
            userAccess.addUser(existing);

            UserData duplicate = new UserData("existingUser", "newPassword123", "new@example.com");

            assertThrows(
                    DataAccessException.class,
                    () -> userAccess.addUser(duplicate),
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

            UserData retrieved = userAccess.getUser("testUser");
            assertNotNull(retrieved, "User should be found in the database.");
            assertEquals("testUser", retrieved.username());
            assertEquals("testuser@example.com", retrieved.email());
        } catch (DataAccessException e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Get User - Negative Case (User Does Not Exist)")
    public void testGetNonExistentUser() {
        try {
            UserData retrieved = userAccess.getUser("nonExistentUser");
            assertNull(retrieved, "User should not be found in the database.");
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
        } catch (Exception e) {
            fail("Exception should not be thrown during test execution: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Authenticate User - Negative Case (Incorrect Password)")
    public void testAuthenticateUserIncorrectPassword() {
        try {
            UserData newUser = new UserData("testUser", "password123", "testuser@example.com");
            userAccess.addUser(newUser);

            assertThrows(
                    DataAccessException.class,
                    () -> userAccess.authenticateUser("testUser", "wrongPassword"),
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

            UserData retrieved = userAccess.getUser("testUser");
            assertNull(retrieved, "User should be deleted from the database.");
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