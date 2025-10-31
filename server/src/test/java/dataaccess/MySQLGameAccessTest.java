package dataaccess;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import dataaccess.DatabaseManager;
import model.GameData;
import model.UserData;
import chess.ChessGame;
import java.util.HashSet;

public class MySQLGameAccessTest {

    private static MySQLGameAccess gameAccess;
    private static MySQLUserAccess userAccess;

    @BeforeAll
    public static void setup() throws DataAccessException {
        DatabaseManager.createDatabase();
        DatabaseManager.createTables();
        gameAccess = new MySQLGameAccess();
        userAccess = new MySQLUserAccess();
    }

    @BeforeEach
    void init() throws DataAccessException {
        gameAccess.clear();
        userAccess.clear();  // Use static instance
    }

    // Helper method
    private GameData findGameByName(String name) throws DataAccessException {
        for (GameData g : gameAccess.listGames()) {
            if (name.equals(g.gameName())) {
                return g;
            }
        }
        return null;
    }

    @Test
    @DisplayName("Test Create Game - Positive Case")
    public void testCreateGame() {
        try {
            ChessGame cg = new ChessGame();
            cg.resetBoard();
            GameData gd = new GameData(cg, 0, "My Game", "whitePlayer", null);
            gameAccess.createGame(gd);

            GameData retrieved = findGameByName("My Game");
            assertNotNull(retrieved, "Game should be retrieved.");
            assertEquals("My Game", retrieved.gameName());

        } catch (DataAccessException e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Get Game - Positive Case")
    public void testGetGame() {
        try {
            userAccess.addUser(new UserData("whitePlayer", "password123", "white@example.com"));

            ChessGame cg = new ChessGame();
            cg.resetBoard();
            GameData gd = new GameData(cg, 0, "My Game", "whitePlayer", null);
            gameAccess.createGame(gd);

            GameData created = findGameByName("My Game");
            assertNotNull(created);

            GameData retrieved = gameAccess.getGame(created.gameID());
            assertNotNull(retrieved);
            assertEquals("My Game", retrieved.gameName());  // Fixed

        } catch (DataAccessException e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Get Game - Negative Case (Game Not Found)")
    public void testGetGameNotFound() {
        try {
            assertNull(gameAccess.getGame(999));
        } catch (DataAccessException e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Game Exists - Positive Case")
    public void testGameExists() {
        try {
            userAccess.addUser(new UserData("whitePlayer", "password123", "white@example.com"));

            ChessGame cg = new ChessGame();
            cg.resetBoard();
            GameData gd = new GameData(cg, 0, "My Game", "whitePlayer", null);
            gameAccess.createGame(gd);

            GameData created = findGameByName("My Game");
            assertNotNull(created);

            assertTrue(gameAccess.gameExists(created.gameID()));

        } catch (DataAccessException e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Update Game - Positive Case")
    public void testUpdateGame() {
        try {
            userAccess.addUser(new UserData("whitePlayer", "pw", "w@x.com"));

            ChessGame cg = new ChessGame();
            cg.resetBoard();
            GameData original = new GameData(cg, 0, "Original Name", "whitePlayer", null);
            gameAccess.createGame(original);

            GameData created = findGameByName("Original Name");
            assertNotNull(created);

            GameData toUpdate = new GameData(
                created.game(),
                created.gameID(),
                "Updated Chess Game",
                created.whiteUsername(),
                created.blackUsername()
            );
            gameAccess.updateGame(toUpdate);

            GameData updated = gameAccess.getGame(created.gameID());
            assertEquals("Updated Chess Game", updated.gameName());

        } catch (DataAccessException e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test List Games - Positive Case")
    public void testListGames() {
        try {
            userAccess.addUser(new UserData("player1", "pass", "e1@a.com"));
            userAccess.addUser(new UserData("player2", "pass", "e2@a.com"));

            ChessGame cg1 = new ChessGame();
            cg1.resetBoard();
            ChessGame cg2 = new ChessGame();
            cg2.resetBoard();

            gameAccess.createGame(new GameData(cg1, 0, "Game A", "player1", null));
            gameAccess.createGame(new GameData(cg2, 0, "Game B", null, "player2"));

            assertEquals(2, gameAccess.listGames().size());

        } catch (DataAccessException e) {
            e.printStackTrace();
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Test Clear - Positive Case (Clear Games)")
    public void testClearGames() {
        try {
            ChessGame cg = new ChessGame();
            cg.resetBoard();
            GameData game1 = new GameData(cg, 0, "Chess Game", "whitePlayer", null);
            gameAccess.createGame(game1);

            assertEquals(1, gameAccess.listGames().size());

            gameAccess.clear();

            assertTrue(gameAccess.listGames().isEmpty());

        } catch (DataAccessException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @AfterEach
    public void tearDown() {
        try {
            gameAccess.clear();
            userAccess.clear();
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }
}