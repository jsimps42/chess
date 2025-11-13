package client;

import exception.ResponseException;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;

public class ServerFacadeTests {

    private static Server server;
    static ServerFacade serverFacade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        var serverUrl = "http://localhost:" + port;
        serverFacade = new ServerFacade(serverUrl);
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        serverFacade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

    @Test
    @DisplayName("Positive: Register new user")
    void registerSuccess() throws Exception {
        AuthData auth = serverFacade.register("newuser", "password123", "new@user.com");

        assertNotNull(auth);
        assertEquals("newuser", auth.username());
        assertTrue(auth.authToken().length() > 10, "Auth token should be reasonably long");
    }

    @Test
    @DisplayName("Negative: Register duplicate username")
    void registerDuplicateFails() throws Exception {
        serverFacade.register("duplicate", "pass", "d@up.com");

        ResponseException exception = assertThrows(ResponseException.class, () ->
          serverFacade.register("duplicate", "newpass", "other@email.com"));
        assertEquals(403, exception.getStatusCode());
    }

    @Test
    @DisplayName("Positive: Login with valid credentials")
    void loginSuccess() throws Exception {
        serverFacade.register("loginuser", "secret", "login@chess.com");

        
        serverFacade.logout();
        AuthData auth = serverFacade.login("loginuser", "secret");

        assertNotNull(auth);
        assertEquals("loginuser", auth.username());
        assertTrue(auth.authToken().length() > 10);
    }

    @Test
    @DisplayName("Negative: Login with wrong password")
    void loginWrongPasswordFails() {
        assertThrows(ResponseException.class, () ->
                serverFacade.login("nonexistent", "wrongpass"), "Should fail on invalid credentials");

        assertDoesNotThrow(() -> serverFacade.register("badpass", "correct", "bp@chess.com"));

        ResponseException ex = assertThrows(ResponseException.class, () ->
                serverFacade.login("badpass", "wrong"));

        assertEquals(401, ex.getStatusCode());
    }

    @Test
    @DisplayName("Positive: Logout after login")
    void logoutSuccess() throws Exception {
        serverFacade.register("logoutuser", "pass", "out@chess.com");
        assertDoesNotThrow(() -> serverFacade.logout());
    }

    @Test
    @DisplayName("Negative: Logout without being logged in")
    void logoutNotLoggedInFails() {
        ResponseException ex = assertThrows(ResponseException.class, () -> serverFacade.logout());
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    @DisplayName("Positive: Create game when logged in")
    void createGameSuccess() throws Exception {
        serverFacade.register("creator", "pass", "c@chess.com");
        GameData game = new GameData(null, 0, "Test Game", null, null);

        assertDoesNotThrow(() -> serverFacade.createGame(game));
    }

    @Test
    @DisplayName("Negative: Create game when not logged in")
    void createGameNotLoggedInFails() {
        GameData game = new GameData(null, 0, "Test Game", null, null);
        ResponseException ex = assertThrows(ResponseException.class, () ->
                serverFacade.createGame(game));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    @DisplayName("Positive: List games returns empty list initially")
    void listGamesEmptySuccess() throws Exception {
        serverFacade.register("lister", "pass", "list@chess.com");
        GamesList games = serverFacade.listGames();

        assertNotNull(games);
        assertTrue(games.games().isEmpty() || games.games() instanceof HashSet<GameData>);
    }

    @Test
    @DisplayName("Positive: List games after creating one")
    void listGamesWithDataSuccess() throws Exception {
        serverFacade.register("gamecreator", "pass", "gc@chess.com");
        serverFacade.createGame(new GameData(null, 0, "Test Game", null, null));

        GamesList games = serverFacade.listGames();
        assertEquals(1, games.games().size());
        assertEquals("Test Game", games.games().iterator().next().gameName());
    }

    @Test
    @DisplayName("Negative: List games when not logged in")
    void listGamesNotLoggedInFails() {
        ResponseException ex = assertThrows(ResponseException.class, () -> serverFacade.listGames());
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    @DisplayName("Positive: Join game as white")
    void joinGameSuccess() throws Exception {
        serverFacade.register("joiner", "pass", "j@chess.com");
        GameData game = new GameData(null, 0, "Test Game", null, null);
        serverFacade.createGame(game);

        GamesList list = serverFacade.listGames();
        int gameID = list.games().iterator().next().gameID();

        assertDoesNotThrow(() -> serverFacade.joinGame(gameID, "WHITE", "joiner"));
    }

    @Test
    @DisplayName("Negative: Join game with invalid ID")
    void joinGameInvalidIDFails() throws Exception {
        serverFacade.register("badjoin", "pass", "bj@chess.com");
        ResponseException ex = assertThrows(ResponseException.class, () ->
                serverFacade.joinGame(9999, "WHITE", "badjoin"));
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    @DisplayName("Negative: Join already taken color")
    void joinGameColorTakenFails() throws Exception {
        serverFacade.register("p1", "pass", "p1@chess.com");
        serverFacade.createGame(new GameData(null, 0, "Test Game", null, null));
        int gameID = serverFacade.listGames().games().iterator().next().gameID();
        serverFacade.joinGame(gameID, "WHITE", "p1");

        serverFacade.logout();
        serverFacade.register("p2", "pass", "p2@chess.com");

        ResponseException ex = assertThrows(ResponseException.class, () ->
                serverFacade.joinGame(gameID, "WHITE", "p2"));
        assertEquals(403, ex.getStatusCode());
    }

    @Test
    @DisplayName("Positive: Observe existing game")
    void observeGameSuccess() throws Exception {
        serverFacade.register("observer", "pass", "obs@chess.com");
        serverFacade.createGame(new GameData(null, 0, "Test Game", null, null));
        int gameID = serverFacade.listGames().games().iterator().next().gameID();

        assertDoesNotThrow(() -> serverFacade.observeGame(gameID));
    }

    @Test
    @DisplayName("Negative: Observe non-existent game")
    void observeGameInvalidIDFails() throws Exception {
        serverFacade.register("badobs", "pass", "bo@chess.com");
        ResponseException ex = assertThrows(ResponseException.class, () ->
          serverFacade.observeGame(9999));
        assertEquals(400, ex.getStatusCode());
    }

    @Test
    @DisplayName("Positive: Clear removes all data")
    void clearSuccess() throws Exception {
        serverFacade.register("todelete", "pass", "del@chess.com");
        serverFacade.createGame(new GameData(null, 0, "Temp Game", null, null));
        assertDoesNotThrow(() -> serverFacade.clear());

        ResponseException ex = assertThrows(ResponseException.class, () ->
                serverFacade.login("todelete", "pass"));
        assertEquals(401, ex.getStatusCode());
    }

    @Test
    @DisplayName("Negative: Clear is always allowed (no auth required)")
    void clearNoAuthRequired() {
        assertDoesNotThrow(() -> serverFacade.clear());
    }
}
