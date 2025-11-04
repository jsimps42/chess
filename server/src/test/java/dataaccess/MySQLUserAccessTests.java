package dataaccess;

import model.UserData;
import passoff.server.TestServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import static org.junit.jupiter.api.Assertions.*;
import passoff.server.TestServer;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(TestServer.class)
class MySQLUserAccessTests {

    private static MySQLUserAccess userAccess;

    @BeforeAll
    static void init() throws DataAccessException {
        userAccess = new MySQLUserAccess();
    }

    @BeforeEach
    @AfterEach
    void clear() throws DataAccessException {
        userAccess.clear();
    }

    @Test
    @DisplayName("addUser - Positive: Successfully adds new user")
    void addUserSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "secret123", "alice@example.com");
        assertDoesNotThrow(() -> userAccess.addUser(user));

        UserData retrieved = userAccess.getUser("alice");
        assertNotNull(retrieved);
        assertEquals("alice", retrieved.username());
        assertTrue(BCrypt.checkpw("secret123", retrieved.password()));
        assertEquals("alice@example.com", retrieved.email());
    }

    @Test
    @DisplayName("addUser - Negative: Duplicate username throws DataAccessException")
    void addUserDuplicateUsername() throws DataAccessException {
        UserData user = new UserData("bob", "pass123", "bob@example.com");
        userAccess.addUser(user);

        UserData duplicate = new UserData("bob", "otherpass", "bob2@example.com");
        assertThrows(DataAccessException.class, () -> userAccess.addUser(duplicate));
    }

    @Test
    @DisplayName("getUser - Positive: Returns existing user")
    void getUserExists() throws DataAccessException {
        UserData user = new UserData("charlie", "mypass", "c@example.com");
        userAccess.addUser(user);

        UserData found = userAccess.getUser("charlie");
        assertNotNull(found);
        assertEquals("charlie", found.username());
        assertTrue(BCrypt.checkpw("mypass", found.password()));
    }

    @Test
    @DisplayName("getUser - Negative: Returns null for non-existent user")
    void getUserNotExists() throws DataAccessException {
        UserData found = userAccess.getUser("nonexistent");
        assertNull(found);
    }

    @Test
    @DisplayName("authenticateUser - Positive: Correct password returns true")
    void authenticateUserCorrectPassword() throws DataAccessException {
        UserData user = new UserData("dave", "correcthorse", "dave@example.com");
        userAccess.addUser(user);

        assertTrue(userAccess.authenticateUser("dave", "correcthorse"));
    }

    @Test
    @DisplayName("authenticateUser - Negative: Wrong password returns false")
    void authenticateUserWrongPassword() throws DataAccessException {
        UserData user = new UserData("eve", "battery", "eve@example.com");
        userAccess.addUser(user);

        assertFalse(userAccess.authenticateUser("eve", "wrongpass"));
    }

    @Test
    @DisplayName("authenticateUser - Negative: Non-existent user returns false")
    void authenticateUserNonExistent() throws DataAccessException {
        assertFalse(userAccess.authenticateUser("ghost", "anypass"));
    }

    @Test
    @DisplayName("clear - Positive: Removes all users")
    void clearRemovesAll() throws DataAccessException {
        userAccess.addUser(new UserData("user1", "p1", "u1@example.com"));
        userAccess.addUser(new UserData("user2", "p2", "u2@example.com"));

        userAccess.clear();

        assertNull(userAccess.getUser("user1"));
        assertNull(userAccess.getUser("user2"));
    }
}