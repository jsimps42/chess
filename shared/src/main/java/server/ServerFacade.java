package server;

import com.google.gson.Gson;
import model.*;
import exception.ResponseException;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashSet;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void register(String username, String password, String email) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void login(String username, String password) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void logout(String authToken) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void createGame(GameData game) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public HashSet<GameData> listGames(String authToken) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void joinGame(String authToken, int GameID, String color) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }

    public void clear(String authToken) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}