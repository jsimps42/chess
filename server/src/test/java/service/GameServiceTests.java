package service;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import dataaccess.*;
import model.*;
import java.util.HashSet;
import chess.ChessGame;

//These are unit tests Tests tests Tests
public class GameServiceTests {
    GameService gameService;
    GameAccess gameAccess;
    AuthAccess authAccess;
    String existingAuthToken;

    @BeforeEach
    void setup() throws DataAccessException{
        gameAccess = new MemoryGameAccess();
        authAccess = new MemoryAuthAccess();
        gameService = new GameService(gameAccess, authAccess);

        AuthData auth = new AuthData("user1", "token123");
        authAccess.addAuth(auth);
        existingAuthToken = auth.authToken();
    }

    @Test
    @DisplayName("Join Game Success")
    void joinGameSuccess() throws Exception {
        int gameID = gameService.createGame(existingAuthToken, "Game");
        boolean success = gameService.joinGame(existingAuthToken, gameID, ChessGame.TeamColor.WHITE);
        assertTrue(success);
    }

    @Test
    @DisplayName("Join Game Invalid Game ID")
    void joinGameInvalidGameID() {
        assertThrows(BadRequestException.class, () -> 
            gameService.joinGame(existingAuthToken, 9999, ChessGame.TeamColor.WHITE));
    }

    @Test
    @DisplayName("Join Game Unauthorized")
    void joinGameUnauthorized() throws Exception {
        int gameID = gameService.createGame(existingAuthToken, "Game");
        assertThrows(UnauthorizedException.class, () -> 
            gameService.joinGame("badtoken", gameID, ChessGame.TeamColor.WHITE));
    }

    @Test
    @DisplayName("Join Game Bad Game ID")
    void joinGameBadRequest() {
        assertThrows(BadRequestException.class, () -> 
            gameService.joinGame(existingAuthToken, 9999, ChessGame.TeamColor.WHITE));
    }

    @Test
    @DisplayName("Join Game Spot Taken")
    void joinGameSpotTaken() throws Exception {
        int gameID = gameService.createGame(existingAuthToken, "Game");
        gameService.joinGame(existingAuthToken, gameID, ChessGame.TeamColor.WHITE);

        AuthData auth2 = new AuthData("user2", "token456");
        authAccess.addAuth(auth2);

        boolean success = gameService.joinGame(auth2.authToken(), gameID, ChessGame.TeamColor.WHITE);
        assertFalse(success);
    }
}