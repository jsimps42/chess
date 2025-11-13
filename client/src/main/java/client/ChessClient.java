package client;

import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;
import com.google.gson.Gson;
import chess.ChessGame;
import model.*;
import exception.ResponseException;
import server.ServerFacade;

import static ui.EscapeSequences.*;

public class ChessClient {
    
    private String username = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    public ChessClient(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println(LOGO + " ♕ Welcome to chess. Sign in to start.");
        System.out.println(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.println(BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.println(msg);
            }
        }

        System.out.println();
    }

    private void printPrompt() {
        System.out.print("\n" + RESET + ">>> " + GREEN);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);

            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "join" -> joinGame(params);
                case "observe" -> observeGame(params);
                case "logout" -> logout();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws Exception {
        if (params.length >= 3) {
            state = State.SIGNEDIN;
            this.username = params[0];
            String password = params[1];
            String email = params[2];
            server.register(username, password, email);
            return String.format("Registration complete. Successfully signed in as \"%s\".", username);
        }
        throw new Exception("Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }

    public String login(String... params) throws Exception {
        if (params.length >= 2) {
            state = State.SIGNEDIN;
            this.username = params[0];
            String password = params[1];
            server.login(username, password);
            return String.format("Successfully signed in as \"%s\".", username);
        }
        throw new Exception("Expected: <USERNAME> <PASSWORD>");
    }

    public String createGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length >= 1) {
            String gameName = params[0];
            server.createGame(new GameData(new ChessGame(), 0, gameName, null, null));
            return String.format("Game \"%s\" successfully created.", gameName);
        }
        throw new Exception("Expected: <NAME>");
    }

    public String listGames() throws Exception {
        assertSignedIn();
        GamesList gameList = server.listGames();
        var result = new StringBuilder();
        var gson = new Gson();
        for (GameData game : gameList) {
            result.append(gson.toJson(game)).append('\n');
        }
        return result.toString();
    }

    public String joinGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length >= 2) {
            int gameID = Integer.parseInt(params[0]);
            String teamColor = params[1];
            server.joinGame(gameID, teamColor);
            return String.format("You joined game %s as %s.", gameID, teamColor);
        }
        throw new Exception("Expected: <ID> <WHITE|BLACK>");
    }

    public String observeGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length >= 1) {
            int gameID = Integer.parseInt(params[0]);
            server.observeGame(gameID);
            return String.format("You are observing game %s.", gameID);
        }
        throw new Exception("Expected: <ID>");
    }

    public String logout() throws Exception {
        assertSignedIn();
        state = State.SIGNEDOUT;
        server.logout();
        String previousUser = this.username;
        this.username = null;
        return String.format("\"%s\" successfully signed out. Thank you for playing.", previousUser);
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - playing chess
                    help - with possible commands
                    """;
        }
        return """
                create <NAME> - a game
                list - games
                join <ID> [WHITE|BLACK] - a game
                observe <ID> - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands
                """;
    }

    private void assertSignedIn() throws Exception {
        if (state == State.SIGNEDOUT) {
            throw new Exception("You must be signed in to run that command");
        }
    }
    
    public enum State {
        SIGNEDOUT,
        SIGNEDIN
    }
}