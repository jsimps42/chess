package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MySQLAuthAccessTests {

    private static MySQLAuthAccess authAccess;
    private static final String TEST_TOKEN = "auth-12345";
    private static final String TEST_USER = "testuser";

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
    void addAuth_Success() throws Exception {
        AuthData auth = new AuthData(TEST_TOKEN, TEST_USER);
        assertDoesNotThrow(() -> authAccess.addAuth(auth));

        AuthData retrieved = authAccess.getAuth(TEST_TOKEN);
        assertEquals(TEST_TOKEN, retrieved.authToken());
        assertEquals(TEST_USER, retrieved.username());
    }

    @Test
    @DisplayName("addAuth - Negative: Duplicate token throws exception")
    void addAuth_DuplicateToken() throws Exception {
        AuthData auth1 = new AuthData(TEST_TOKEN, "user1");
        authAccess.addAuth(auth1);

        AuthData auth2 = new AuthData(TEST_TOKEN, "user2");
        assertThrows(DataAccessException.class, () -> authAccess.addAuth(auth2));
    }

    @Test
    @DisplayName("getAuth - Positive: Returns valid auth")
    void getAuth_Exists() throws Exception {
        AuthData auth = new AuthData("valid-token", "alice");
        authAccess.addAuth(auth);

        AuthData found = authAccess.getAuth("valid-token");
        assertNotNull(found);
        assertEquals("alice", found.username());
    }

    @Test
    @DisplayName("getAuth - Negative: Returns null for invalid token")
    void getAuth_NotExists() throws Exception {
        assertNull(authAccess.getAuth("invalid-token"));
    }

    @Test
    @DisplayName("deleteAuth - Positive: Removes auth token")
    void deleteAuth_Success() throws Exception {
        AuthData auth = new AuthData("delete-me", "bob");
        authAccess.addAuth(auth);

        assertDoesNotThrow(() -> authAccess.deleteAuth("delete-me"));
        assertNull(authAccess.getAuth("delete-me"));
    }

    @Test
    @DisplayName("deleteAuth - Negative: No error on missing token")
    void deleteAuth_MissingToken() throws Exception {
        assertDoesNotThrow(() -> authAccess.deleteAuth("nonexistent"));
    }

    @Test
    @DisplayName("clear - Positive: Removes all auth tokens")
    void clear_RemovesAll() throws Exception {
        authAccess.addAuth(new AuthData("t1", "u1"));
        authAccess.addAuth(new AuthData("t2", "u2"));

        authAccess.clear();

        assertNull(authAccess.getAuth("t1"));
        assertNull(authAccess.getAuth("t2"));
    }
}