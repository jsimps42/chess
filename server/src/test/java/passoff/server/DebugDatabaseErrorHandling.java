package passoff.server;

import org.junit.jupiter.api.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DebugDatabaseErrorHandling {

    private static Server server;
    private static int port;
    private static String baseUrl;

    @BeforeAll
    static void startServer() throws Exception {
        server = new Server();
        port = server.run(0);
        baseUrl = "http://localhost:" + port;
        System.out.println("Server started on port: " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @DisplayName("DEBUG: Reproduce databaseErrorHandling")
    void debugDatabaseErrorHandling() throws Exception {
        breakDatabaseConnection();

        // === Test each endpoint ===
        testEndpoint("DELETE", "/db", null, "CLEAR");
        testEndpoint("POST", "/user", "{\"username\":\"test\",\"password\":\"pass\",\"email\":\"e\"}", "REGISTER");
        testEndpoint("POST", "/session", "{\"username\":\"test\",\"password\":\"pass\"}", "LOGIN");
        testEndpoint("DELETE", "/session", null, "LOGOUT", "auth-token");
        testEndpoint("POST", "/game", "{\"gameName\":\"test\"}", "CREATE_GAME", "auth-token");
        testEndpoint("GET", "/game", null, "LIST_GAMES", "auth-token");
        testEndpoint("PUT", "/game", "{\"playerColor\":\"WHITE\",\"gameID\":1}", "JOIN_GAME", "auth-token");
    }

    private void testEndpoint(String method, String path, String jsonBody, String name, String... headers) {
        try {
            URL url = new URL(baseUrl + path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            // Add auth header if provided
            if (headers.length > 0) {
                conn.setRequestProperty("Authorization", headers[0]);
            }

            if (jsonBody != null) {
                conn.setDoOutput(true);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }

            int status = conn.getResponseCode();
            String body = readResponseBody(conn);

            System.out.println(name + " -> Status: " + status + " | Body: " + body);
            assertEquals(500, status, name + " should return 500");

        } catch (Exception e) {
            System.out.println(name + " -> EXCEPTION: " + e.getMessage());
        }
    }

    private String readResponseBody(HttpURLConnection conn) throws IOException {
        InputStream stream = conn.getResponseCode() >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (stream == null) {
            return "";
        }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream))) {
            return br.lines().collect(java.util.stream.Collectors.joining());
        }
    }

    private void breakDatabaseConnection() throws Exception {
        Class<?> dbManagerClass = Class.forName("dataaccess.DatabaseManager");
        java.lang.reflect.Method loadProps = dbManagerClass.getDeclaredMethod("loadProperties", Properties.class);
        loadProps.setAccessible(true);

        Properties fake = new Properties();
        fake.setProperty("db.name", UUID.randomUUID().toString());
        fake.setProperty("db.user", UUID.randomUUID().toString());
        fake.setProperty("db.password", UUID.randomUUID().toString());
        fake.setProperty("db.host", "localhost");
        fake.setProperty("db.port", "99999");

        Object instance = dbManagerClass.getDeclaredConstructor().newInstance();
        loadProps.invoke(instance, fake);

        System.out.println("Database connection intentionally broken");
    }
}