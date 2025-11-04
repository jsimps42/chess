package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import passoff.server.TestServer;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(TestServer.class)
class MySQLAuthAccessTests {

    private static MySQLAuthAccess authAccess;
    private static final String testToken = "auth-12345";
    private static final String testUser = "testuser";

    @BeforeAll
    static void init() throws DataAccessException {
        
        authAccess = new MySQLAuthAccess();
    }

    @BeforeEach
    @AfterEach
    void clear() throws Exception {
        authAccess.clear();
    }

    @Test
    @DisplayName("addAuth - Positive: Adds auth token")
    void addAuthSuccess() throws Exception {
        AuthData auth = new AuthData(testToken, testUser);
        assertDoesNotThrow(() -> authAccess.addAuth(auth));

        AuthData retrieved = authAccess.getAuth(testToken);
        assertEquals(testToken, retrieved.authToken());
        assertEquals(testUser, retrieved.username());
    }

    @Test
    @DisplayName("addAuth - Negative: Duplicate token throws exception")
    void addAuthDuplicateToken() throws Exception {
        AuthData auth1 = new AuthData(testToken, "user1");
        authAccess.addAuth(auth1);

        AuthData auth2 = new AuthData(testToken, "user2");
        assertThrows(DataAccessException.class, () -> authAccess.addAuth(auth2));
    }

    @Test
    @DisplayName("getAuth - Positive: Returns valid auth")
    void getAuthExists() throws Exception {
        AuthData auth = new AuthData("valid-token", "alice");
        authAccess.addAuth(auth);

        AuthData found = authAccess.getAuth("valid-token");
        assertNotNull(found);
        assertEquals("alice", found.username());
    }

    @Test
    @DisplayName("getAuth - Negative: Returns null for invalid token")
    void getAuthNotExists() throws Exception {
        assertNull(authAccess.getAuth("invalid-token"));
    }

    @Test
    @DisplayName("deleteAuth - Positive: Removes auth token")
    void deleteAuthSuccess() throws Exception {
        AuthData auth = new AuthData("delete-me", "bob");
        authAccess.addAuth(auth);

        assertDoesNotThrow(() -> authAccess.deleteAuth("delete-me"));
        assertNull(authAccess.getAuth("delete-me"));
    }

    @Test
    @DisplayName("deleteAuth - Negative: No error on missing token")
    void deleteAuthMissingToken() throws Exception {
        assertDoesNotThrow(() -> authAccess.deleteAuth("nonexistent"));
    }

    @Test
    @DisplayName("clear - Positive: Removes all auth tokens")
    void clearRemovesAll() throws Exception {
        authAccess.addAuth(new AuthData("t1", "u1"));
        authAccess.addAuth(new AuthData("t2", "u2"));

        authAccess.clear();

        assertNull(authAccess.getAuth("t1"));
        assertNull(authAccess.getAuth("t2"));
    }
}