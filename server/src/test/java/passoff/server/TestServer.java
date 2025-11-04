package passoff.server;

import server.Server;                     // <-- your production Server
import java.io.IOException;
import java.net.ServerSocket;

/**
 * JUnit 5 extension that starts the real {@link Server} on a random free port
 * before the first test class and stops it after the last test class.
 *
 * <p>Just put {@code @ExtendWith(TestServer.class)} on every DAO test class
 * (or on a common base class) and the server will be up while the MySQL DAOs
 * are exercised.
 */
public class TestServer implements org.junit.jupiter.api.extension.BeforeAllCallback,
      org.junit.jupiter.api.extension.AfterAllCallback {

    private static server.Server javalinServer;
    private static int port;

    @Override
    public void beforeAll(org.junit.jupiter.api.extension.ExtensionContext context) throws IOException {
        if (javalinServer != null) return;

        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }

        javalinServer = new Server();
        int actual = javalinServer.run(port);
        if (actual != port) {
            throw new IllegalStateException("Server started on unexpected port " + actual);
        }

        System.out.println("Test server running at http://localhost:" + port);
        waitUntilReady();
    }

    @Override
    public void afterAll(org.junit.jupiter.api.extension.ExtensionContext context) {
        if (javalinServer != null) {
            javalinServer.stop();
            System.out.println("Test server stopped.");
            javalinServer = null;
        }
    }

    private static void waitUntilReady() {
        long deadline = System.currentTimeMillis() + 10_000L;
        while (System.currentTimeMillis() < deadline) {
            try (var ignored = new java.net.Socket("localhost", port)) {
                return;
            } catch (IOException ignored) {
                try { Thread.sleep(50); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            }
        }
        throw new RuntimeException("Server did not become ready within 10 s");
    }

    public static String baseUrl() {
        return "http://localhost:" + port;
    }
}