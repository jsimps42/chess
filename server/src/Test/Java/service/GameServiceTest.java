package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.*;
import model.*;
import java.util.HashSet;

public class GameServiceTest {
    GameService gameService;
    GameAccess gameAccess;
    AuthAccess authAccess;
    String existingAuthToken;

    @BeforeEach
    void setup() {
        gameAccess = new MemoryGameAccess();
        authAccess = new MemoryAuthAccess();
        gameService = new GameService(gameAccess, authAccess);

        AuthData auth = new AuthData("user1", "token123");
        authAccess.addAuth(auth);
        existingAuthToken = auth.authToken();
    }

    @Test
    @DisplayName("Create Game Success")
    void createGameSuccess() throws Exception {
        int gameId = gameService.createGame(existingAuthToken, "ChessGame");
        assertTrue(gameId > 0);
    }

    @Test
    @DisplayName("Create Game Unauthorized")
    void createGameInvalidAuthThrowsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> 
            gameService.createGame("badtoken", "ChessGame"));
    }

    @Test
    @DisplayName("List Games Success")
    void listGamesSuccess() throws Exception {
        gameService.createGame(existingAuthToken, "Game 1");
        HashSet<GameData> games = gameService.listGames(existingAuthToken);
        assertNotNull(games);
        assertEquals(1, games.size());
    }

    @Test
    @DisplayName("List Games Unauthorized")
    void listGamesUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> 
            gameService.listGames("badtoken"));
    }

    @Test
    @DisplayName("Join Game Success")
    void joinGameSuccess() throws Exception {
        int gameID = gameService.createGame(existingAuthToken, "Game");
        boolean success = gameService.joinGame(existingAuthToken, gameID, "WHITE");
        assertTrue(success);
    }

    @Test
    @DisplayName("Join Game Unauthorized")
    void joinGameUnauthorized() throws Exception {
        int gameID = gameService.createGame(existingAuthToken, "Game");
        assertThrows(UnauthorizedException.class, () -> 
            gameService.joinGame("badtoken", gameID, "WHITE"));
    }

    @Test
    @DisplayName("Join Game Bad Game ID")
    void joinGameBadRequest() {
        assertThrows(BadRequestException.class, () -> 
            gameService.joinGame(existingAuthToken, 9999, "WHITE"));
    }

    @Test
    @DisplayName("Join Game Spot Taken")
    void joinGameSpotTaken() throws Exception {
        int gameID = gameService.createGame(existingAuthToken, "Game");
        gameService.joinGame(existingAuthToken, gameID, "WHITE");

        AuthData auth2 = new AuthData("user2", "token456");
        authAccess.addAuth(auth2);

        boolean success = gameService.joinGame(auth2.authToken(), gameID, "WHITE");
        assertFalse(success);
    }
}