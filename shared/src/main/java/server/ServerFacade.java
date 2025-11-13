package server;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import model.*;
import model.GamesList.GamesListResponse;
import exception.ResponseException;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private String authToken;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var request = buildRequest("POST", "/user", new UserData(username, password, email));
        var response = sendRequest(request);
        AuthData auth = handleResponse(response, AuthData.class);
        this.authToken = auth.authToken();
        return auth;
    }

    public AuthData login(String username, String password) throws Exception {
        var request = buildRequest("POST", "/session", new loginRequest(username, password));
        var response = sendRequest(request);
        AuthData auth = handleResponse(response, AuthData.class);
        this.authToken = auth.authToken();
        return auth;
    }

    public void logout() throws Exception {
        var request = buildRequest("DELETE", "/session", null);
        var response = sendRequest(request);
        handleResponse(response, null);
        this.authToken = null;
    }

    public void createGame(GameData game) throws Exception {
        var request = buildRequest("POST", "/game", game);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public GamesList listGames() throws Exception {
        var request = buildRequest("GET", "/game", null);
        var response = sendRequest(request);
        GamesListResponse wrapper = handleResponse(response, GamesListResponse.class);
        return wrapper == null ? new GamesList() : new GamesList(wrapper.games());
    }

    public void joinGame(int gameID, String color, String username) throws Exception {
        var request = buildRequest("PUT", "/game", new joinGameRequest(gameID, color));
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void observeGame(int gameID) throws Exception {
        var request = buildRequest("GET", "/game", gameID);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void clear() throws Exception {
        var request = buildRequest("DELETE", "/db", null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        
        if (authToken != null && !authToken.isBlank()) {
            request.setHeader("Authorization", authToken);
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
                throw ResponseException.fromJson(body, status);
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

    private record loginRequest(String username, String password) {

    }

    private record joinGameRequest(
     @SerializedName("gameID") int gameID,
     @SerializedName("teamColor") String teamColor
    ) {
    }
}