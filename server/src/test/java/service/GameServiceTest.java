package service;

import chess.ChessGame;
import dataaccess.*;
import dataaccess.BadRequestException;
import dataaccess.ForbiddenException;
import dataaccess.UnauthorizedException;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameAccess gameAccess;
    private AuthAccess authAccess;
    private UserAccess userAccess;
    private GameService gameService;
    private UserService userService;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setup() throws Exception {
        gameAccess = new MemoryGameAccess();
        authAccess = new MemoryAuthAccess();
        userAccess = new MemoryUserAccess();

        gameService = new GameService(gameAccess, authAccess);
        userService = new UserService(userAccess, authAccess);

        tokenA = userService.createUser(new UserData("taylor", "12345", "a@a.a")).authToken();
        tokenB = userService.createUser(new UserData("rolyat", "54321", "b@b.b")).authToken();
    }

    @Test
    void listGamesSuccess() throws Exception {
        int id1 = gameService.createGame(tokenA, "g1");
        int id2 = gameService.createGame(tokenA, "g2");
        HashSet<GameData> games = gameService.listGames(tokenA);
        assertNotNull(games);
        assertTrue(games.stream().anyMatch(g -> g.gameID() == id1));
        assertTrue(games.stream().anyMatch(g -> g.gameID() == id2));
    }

    @Test
    void listGamesUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("bad-token"));
    }

    @Test
    void getGameDataSuccess() throws Exception {
        int id = gameService.createGame(tokenA, "g1");
        GameData g = gameService.getGameData(tokenA, id);
        assertEquals(id, g.gameID());
        assertEquals("g1", g.gameName());
        assertNotNull(g.game());
    }

    @Test
    void getGameDataBadId() {
        assertThrows(BadRequestException.class, () -> gameService.getGameData(tokenA, 9999));
    }

    @Test
    void updateGameSuccess() throws Exception {
        int id = gameService.createGame(tokenA, "update");
        GameData before = gameAccess.getGame(id);
        assertNull(before.whiteUsername());
        assertNull(before.blackUsername());

        GameData after = new GameData(before.game(), id, before.gameName(), "test", "dummy");
        gameService.updateGame(tokenA, after);

        GameData stored = gameAccess.getGame(id);
        assertEquals("test", stored.whiteUsername());
        assertEquals("dummy", stored.blackUsername());
    }

    @Test
    void updateGameNonexistentId() {
        GameData fake = new GameData(new ChessGame(), 42, "nope", null, null);
        assertThrows(BadRequestException.class, () -> gameService.updateGame(tokenA, fake));
    }

    @Test
    void createGameSuccess() throws Exception {
        int id = gameService.createGame(tokenA, "newGame");
        assertTrue(id > 0);
        GameData g = gameAccess.getGame(id);
        assertEquals("newGame", g.gameName());
        assertNotNull(g.game());
    }

    @Test
    void createGameUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> gameService.createGame("ahhhhhhhhh", "no"));
    }

    @Test
    void joinGameSuccessWhite() throws Exception {
        int id = gameService.createGame(tokenA, "game1");
        gameService.joinGame(tokenA, id, "WHITE");
        GameData g = gameAccess.getGame(id);
        assertEquals("test", g.whiteUsername());
        assertNull(g.blackUsername());
    }

    @Test
    void joinGameSuccessBlack() throws Exception {
        int id = gameService.createGame(tokenB, "game2");
        gameService.joinGame(tokenB, id, "BLACK");
        GameData g = gameAccess.getGame(id);
        assertEquals("dummy", g.blackUsername());
        assertNull(g.whiteUsername());
    }

    @Test
    void joinGameStealColor() throws Exception {
        int id = gameService.createGame(tokenA, "mine");
        gameService.joinGame(tokenA, id, "BLACK");
        assertThrows(ForbiddenException.class,
                () -> gameService.joinGame(tokenB, id, "BLACK"));
    }

    @Test
    void joinGameInvalidColor() throws Exception {
        int id = gameService.createGame(tokenA, "colorblind");
        assertThrows(BadRequestException.class,
                () -> gameService.joinGame(tokenA, id, "GREEN"));
        assertThrows(BadRequestException.class,
                () -> gameService.joinGame(tokenA, id, ""));
    }

    @Test
    void joinGameBadId() {
        assertThrows(BadRequestException.class,
                () -> gameService.joinGame(tokenA, 1234, "WHITE"));
    }

    @Test
    void joinGameUnauthorized() {
        assertThrows(UnauthorizedException.class,
                () -> gameService.joinGame("no", 1, "WHITE"));
    }

    @Test
    void clearResetsGames() throws Exception {
        int id = gameService.createGame(tokenA, "clear");
        assertNotNull(gameAccess.getGame(id));
        gameService.clear();
        assertTrue(gameAccess.listGames().isEmpty());
    }
}