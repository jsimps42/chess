package dataaccess;

import model.GameData;
import passoff.server.TestServer;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;
import java.util.HashSet;
import passoff.server.TestServer;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ExtendWith(TestServer.class)
class MySQLGameAccessTests {

    private static MySQLGameAccess gameAccess;
    private static ChessGame defaultGame;

    @BeforeAll
    static void init() throws DataAccessException {
        gameAccess = new MySQLGameAccess();
        defaultGame = new ChessGame();
    }

    @BeforeEach
    @AfterEach
    void clear() throws DataAccessException {
        gameAccess.clear();
    }

    @Test
    @DisplayName("createGame - Positive: Creates new game with auto-increment ID")
    void createGameSuccess() throws DataAccessException {
        GameData game = new GameData(defaultGame, 0, "My Game", null, null);
        assertDoesNotThrow(() -> gameAccess.createGame(game));

        HashSet<GameData> games = gameAccess.listGames();
        assertEquals(1, games.size());
        GameData created = games.iterator().next();
        assertTrue(created.gameID() > 0);
        assertEquals("My Game", created.gameName());
        assertNull(created.whiteUsername());
    }

    @Test
    @DisplayName("createGame - Negative: Duplicate gameName not enforced (allowed)")
    void createGameDuplicateNameAllowed() throws DataAccessException {
        GameData g1 = new GameData(defaultGame, 0, "Same", null, null);
        GameData g2 = new GameData(defaultGame, 0, "Same", null, null);
        gameAccess.createGame(g1);
        assertDoesNotThrow(() -> gameAccess.createGame(g2));
    }

    @Test
    @DisplayName("getGame - Positive: Returns correct game")
    void getGameExists() throws DataAccessException {
        GameData original = new GameData(defaultGame, 0, "TestGame", "white", "black");
        gameAccess.createGame(original);

        int gameID = gameAccess.listGames().iterator().next().gameID();
        GameData retrieved = gameAccess.getGame(gameID);

        assertNotNull(retrieved);
        assertEquals(gameID, retrieved.gameID());
        assertEquals("TestGame", retrieved.gameName());
        assertEquals("white", retrieved.whiteUsername());
    }

    @Test
    @DisplayName("getGame - Negative: Returns null for invalid ID")
    void getGameNotExists() throws DataAccessException {
        assertNull(gameAccess.getGame(999));
    }

    @Test
    @DisplayName("gameExists - Positive: Returns true for existing game")
    void gameExistsTrue() throws DataAccessException {
        GameData game = new GameData(defaultGame, 0, "Exists", null, null);
        gameAccess.createGame(game);
        int gameID = gameAccess.listGames().iterator().next().gameID();
        assertTrue(gameAccess.gameExists(gameID));
    }

    @Test
    @DisplayName("gameExists - Negative: Returns false for non-existent")
    void gameExistsFalse() throws DataAccessException {
        assertFalse(gameAccess.gameExists(999));
    }

    @Test
    @DisplayName("updateGame - Positive: Updates game state and players")
    void updateGameSuccess() throws DataAccessException {
        GameData original = new GameData(defaultGame, 0, "UpdateMe", null, null );
        gameAccess.createGame(original);
        int gameID = gameAccess.listGames().iterator().next().gameID();

        ChessGame updatedChess = new ChessGame();
        try {
            updatedChess.makeMove(new ChessMove(new ChessPosition(1, 1), new ChessPosition(3, 1), null));
        } catch (InvalidMoveException e) {}
        GameData updated = new GameData(updatedChess, gameID, "UpdateMe", "player1", "player2");

        assertDoesNotThrow(() -> gameAccess.updateGame(updated));

        GameData retrieved = gameAccess.getGame(gameID);
        assertEquals("player1", retrieved.whiteUsername());
        assertEquals("player2", retrieved.blackUsername());
        assertNotEquals(defaultGame, retrieved.game());
    }

    @Test
    @DisplayName("updateGame - Negative: Throws on invalid gameID")
    void updateGameInvalidID() {
        ChessGame game = new ChessGame();
        GameData invalid = new GameData(game, 999, "Fake", "a", "b");
        assertThrows(DataAccessException.class, () -> gameAccess.updateGame(invalid));
    }

    @Test
    @DisplayName("listGames - Positive: Returns all games")
    void listGamesMultiple() throws DataAccessException {
        gameAccess.createGame(new GameData(defaultGame, 0, "Game1", null, null));
        gameAccess.createGame(new GameData(defaultGame, 0, "Game2", null, null));

        HashSet<GameData> games = gameAccess.listGames();
        assertEquals(2, games.size());
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Game1")));
        assertTrue(games.stream().anyMatch(g -> g.gameName().equals("Game2")));
    }

    @Test
    @DisplayName("listGames - Negative: Empty list when none exist")
    void listGamesEmpty() throws DataAccessException {
        HashSet<GameData> games = gameAccess.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    @DisplayName("clear - Positive: Removes all games")
    void clearRemovesAll() throws DataAccessException {
        gameAccess.createGame(new GameData(defaultGame, 0, "Temp", null, null));
        assertFalse(gameAccess.listGames().isEmpty());

        gameAccess.clear();

        assertTrue(gameAccess.listGames().isEmpty());
    }
}