package service;

import chess.ChessGame;
import dataaccess.*;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private GameService gameService;
    private GameDAO gameDAO;

    @BeforeEach
    void setup() {
        gameDAO = new MemoryGameDAO();
        gameService = new GameService(gameDAO);
    }

    @Test
    void createGameSuccess() throws Exception {
        GameData createdGame = gameService.createGame("My Game");
        assertEquals("My Game", createdGame.gameName());
        assertTrue(createdGame.gameID() > 0);
    }

    @Test
    void listGamesSuccess() throws Exception {
        gameService.createGame("Game 1");
        gameService.createGame("Game 2");
        Collection<GameData> games = gameService.listGames();
        assertEquals(2, games.size());
    }

}