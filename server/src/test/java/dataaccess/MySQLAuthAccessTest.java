package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class MySQLAuthAccessTest {

    private static MySQLAuthAccess authAccess;
    private static MySQLUserAccess userAccess;

    @BeforeAll
    public static void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
        authAccess = new MySQLAuthAccess();
        userAccess = new MySQLUserAccess();
    }

    @BeforeEach
    void init() throws DataAccessException {
        authAccess.clear();
        userAccess.clear();
    }

    @Test
    @DisplayName("Test Add Auth - Positive Case")
    public void testAddAuth() {
        try {
            userAccess.addUser(new UserData("jake", "pass123", "jake@example.com"));
            AuthData auth = new AuthData("valid-token-123", "jake");
            authAccess.addAuth(auth);
            AuthData retrieved = authAccess.getAuth("valid-token-123");
            assertNotNull(retrieved);
            assertEquals("jake", retrieved.username());
        } catch (DataAccessException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Get Auth - Positive Case")
    public void testGetAuth() {
        try {
            userAccess.addUser(new UserData("alice", "secret", "a@e.com"));
            AuthData auth = new AuthData("token-alice", "alice");
            authAccess.addAuth(auth);
            AuthData found = authAccess.getAuth("token-alice");
            assertNotNull(found);
            assertEquals("alice", found.username());
            assertEquals("token-alice", found.authToken());
        } catch (DataAccessException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Get Auth - Negative Case (Not Found)")
    public void testGetAuthNotFound() {
        try {
            assertNull(authAccess.getAuth("non-existent-token"));
        } catch (DataAccessException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Delete Auth - Positive Case")
    public void testDeleteAuth() {
        try {
            userAccess.addUser(new UserData("bob", "bobpass", "b@e.com"));
            AuthData auth = new AuthData("token-bob", "bob");
            authAccess.addAuth(auth);
            assertNotNull(authAccess.getAuth("token-bob"));
            authAccess.deleteAuth("token-bob");
            assertNull(authAccess.getAuth("token-bob"));
        } catch (DataAccessException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Delete Auth - Negative Case (Not Found)")
    public void testDeleteAuthNotFound() {
        assertThrows(DataAccessException.class, () -> {
            authAccess.deleteAuth("fake-token");
        }, "Should throw when deleting non-existent token");
    }

    @Test
    @DisplayName("Test Clear Auth - Positive Case")
    public void testClearAuth() {
        try {
            userAccess.addUser(new UserData("user1", "p1", "u1@e.com"));
            userAccess.addUser(new UserData("user2", "p2", "u2@e.com"));
            authAccess.addAuth(new AuthData("t1", "user1"));
            authAccess.addAuth(new AuthData("t2", "user2"));
            assertNotNull(authAccess.getAuth("t1"));
            authAccess.clear();
            assertNull(authAccess.getAuth("t1"));
            assertNull(authAccess.getAuth("t2"));
        } catch (DataAccessException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Auth Persists Across Operations")
    public void testAuthPersists() {
        try {
            userAccess.addUser(new UserData("persist", "pw", "p@e.com"));
            String token = "persist-token";
            authAccess.addAuth(new AuthData(token, "persist"));
            authAccess.addAuth(new AuthData("temp", "persist"));
            authAccess.deleteAuth("temp");
            AuthData result = authAccess.getAuth(token);
            assertNotNull(result);
            assertEquals("persist", result.username());
        } catch (DataAccessException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            authAccess.clear();
            userAccess.clear();
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}