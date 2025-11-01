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

    private void addTestUser() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "testuser@example.com");
        userAccess.addUser(user);
    }

    @Test
    @DisplayName("Test Add User - Positive Case")
    public void testAddUser() {
        try {
            addTestUser();
            UserData retrieved = userAccess.getUser("testUser");
            assertNotNull(retrieved);
            assertEquals("testUser", retrieved.username());
            assertEquals("testuser@example.com", retrieved.email());
        } catch (DataAccessException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Add User - Negative Case (Username Already Exists)")
    public void testAddUserWithExistingUsername() {
        try {
            UserData existing = new UserData("existingUser", "password123", "existing@example.com");
            userAccess.addUser(existing);
            UserData duplicate = new UserData("existingUser", "newPassword123", "new@example.com");
            assertThrows(DataAccessException.class, () -> userAccess.addUser(duplicate));
        } catch (DataAccessException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Get User - Positive Case")
    public void testGetUser() {
        try {
            addTestUser();
            UserData retrieved = userAccess.getUser("testUser");
            assertNotNull(retrieved);
            assertEquals("testUser", retrieved.username());
            assertEquals("testuser@example.com", retrieved.email());
        } catch (DataAccessException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Get User - Negative Case (User Does Not Exist)")
    public void testGetNonExistentUser() {
        try {
            assertNull(userAccess.getUser("nonExistentUser"));
        } catch (DataAccessException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Authenticate User - Positive Case")
    public void testAuthenticateUser() {
        try {
            addTestUser();
            userAccess.authenticateUser("testUser", "password123");
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Authenticate User - Negative Case (Incorrect Password)")
    public void testAuthenticateUserIncorrectPassword() {
        try {
            addTestUser();
            assertThrows(DataAccessException.class, () -> userAccess.authenticateUser("testUser", "wrongPassword"));
        } catch (DataAccessException e) {
            fail(e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Clear - Positive Case")
    public void testClear() {
        try {
            addTestUser();
            userAccess.clear();
            assertNull(userAccess.getUser("testUser"));
        } catch (DataAccessException e) {
            fail(e.getMessage());
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