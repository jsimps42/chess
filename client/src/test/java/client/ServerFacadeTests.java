package client;

import exception.ResponseException;
import org.junit.jupiter.api.*;
import server.Server;
import dataaccess.MemoryAuthAccess;
import dataaccess.MemoryGameAccess;
import dataaccess.MemoryUserAccess;
import service.GameService;
import service.UserService;

public class ServerFacadeTests {

    private static Server server;
    static ChessClient client;

    @BeforeAll
    public static void init() throws Exception {
        var userAccess = new MemoryUserAccess();
        var gameAccess = new MemoryGameAccess();
        var authAccess = new MemoryAuthAccess();
        var userService = new UserService(userAccess, authAccess);
        var gameService = new GameService(gameAccess, authAccess); 
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        var serverUrl = "http://localhost:" + port;
        client = new ChessClient(serverUrl);
        client.register("Dummy", "password", "dummy@test.com");
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
