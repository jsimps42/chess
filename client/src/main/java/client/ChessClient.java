package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import chess.ChessGame;
import chess.ChessPosition;
import chess.ChessPiece;
import exception.ResponseException;
import model.*;
import server.ServerFacade;
import ui.ChessBoardUI;
import static ui.EscapeSequences.*;

public class ChessClient {
    private String username = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private final Map<Integer, GameData> gameIDsMap = new HashMap<>();
    private GameData joinedGame = null;

    public ChessClient(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println(LOGO + " Welcome to chess. Sign in to start.");
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
                System.out.println(RED + msg);
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
                case "redraw" -> redrawBoard();
                case "show_moves" -> highlightLegalMoves(params);
                case "move" -> makeMove(params);
                case "leave" -> leave();
                case "resign" -> resign();
                default -> help();
            };
        } catch (Exception ex) {
            return RED + ex.getMessage();
        }
    }

    public String register(String... params) throws Exception {
        if (params.length == 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            server.register(username, password, email);
            this.username = username;
            state = State.SIGNEDIN;
            return String.format("Registration complete. Successfully signed in as \"%s\".", username);
        }
        throw new Exception("Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }

    public String login(String... params) throws Exception {
        if (params.length == 2) {
            String username = params[0];
            String password = params[1];
            server.login(username, password);
            this.username = username;
            state = State.SIGNEDIN;
            return String.format("Successfully signed in as \"%s\".", username);
        }
        throw new Exception("Expected: <USERNAME> <PASSWORD>");
    }

    public String createGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length == 1) {
            String gameName = params[0];
            server.createGame(new GameData(new ChessGame(), 0, gameName, null, null));
            return String.format("Game \"%s\" successfully created.", gameName);
        }
        throw new Exception("Expected: <NAME>");
    }

    public String listGames() throws Exception {
        assertSignedIn();
        GamesList gameList = server.listGames();
        List<GameData> games = new ArrayList<>(gameList.games());
        gameIDsMap.clear();

        System.out.println(SET_TEXT_COLOR_BLUE + "=== Active Games ===" + RESET);

        if (games.isEmpty()) {
            System.out.println(SET_TEXT_COLOR_YELLOW + "No games available." + RESET);
            return "Listed 0 games.";
        }

        for (int i = 0; i < games.size(); i++) {
            GameData game = games.get(i);
            int displayNumber = i + 1;

            gameIDsMap.put(displayNumber, game);

            String white = game.whiteUsername() != null ? game.whiteUsername() : "(empty)";
            String black = game.blackUsername() != null ? game.blackUsername() : "(empty)";

            System.out.printf("%s%d.%s \"%s\" — White: %s%s%s, Black: %s%s%s%n",
              SET_TEXT_BOLD, displayNumber, RESET,
              game.gameName(),
              SET_TEXT_COLOR_BLUE, white, RESET,
              SET_TEXT_COLOR_RED, black, RESET);
        }

        return String.format("Listed %d game%s.", games.size(), games.size() == 1 ? "" : "s");
    }

    public String joinGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length != 2) {
            throw new Exception("Expected: join <ID> [WHITE|BLACK]");
        }

        int displayNumber;
        try {
            displayNumber = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid ID – refer to list and select a game ID");
        }

        GameData game = gameIDsMap.get(displayNumber);
        if (game == null) {
            throw new Exception("Invalid game ID – use 'list' to see available games");
        }

        String requestedColor = params.length == 2 ? params[1].toUpperCase() : null;

        String white = game.whiteUsername();
        String black = game.blackUsername();
        boolean isWhite = username != null && username.equals(white);
        boolean isBlack = username != null && username.equals(black);
        boolean alreadyInGame = isWhite || isBlack;

        String chosenColor = null;
        ChessGame.TeamColor perspective = ChessGame.TeamColor.WHITE;

        if (requestedColor != null) {
            if (!List.of("WHITE", "BLACK").contains(requestedColor)) {
                throw new Exception("Color must be WHITE or BLACK");
            }

            boolean spotTaken = (requestedColor.equals("WHITE") && white != null && !isWhite) ||
              (requestedColor.equals("BLACK") && black != null && !isBlack);

            if (spotTaken) {
                throw new Exception("That color is already taken by another player");
            }

            chosenColor = requestedColor;
            perspective = requestedColor.equals("BLACK") 
              ? ChessGame.TeamColor.BLACK 
              : ChessGame.TeamColor.WHITE;
        } else {
            if (alreadyInGame) {
                chosenColor = isWhite ? "WHITE" : "BLACK";
                perspective = isWhite ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
            } else {
                chosenColor = null;
                perspective = ChessGame.TeamColor.WHITE;
            }
        }

        if (chosenColor != null) {
            try {
                server.joinGame(game.gameID(), chosenColor);
                state = State.PLAYER;
                joinedGame = game;
            } catch (ResponseException e) {
                if (e.getMessage().contains("already taken") && alreadyInGame) {
                } else {
                    throw e;
                }
            }
        }

        ChessBoardUI.drawBoard(game.game(), perspective);

        if (chosenColor != null) {
            if (alreadyInGame) {
                return String.format("Rejoined game \"%s\" as %s", game.gameName(), chosenColor);
            } else {
                return String.format("Joined game \"%s\" as %s", game.gameName(), chosenColor);
            }
        } else {
            return String.format("Now observing game \"%s\"", game.gameName());
        }
    }

    public String observeGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length != 1) {
            throw new Exception("Expected: <ID>");
        }

        int displayNumber;
        try {
            displayNumber = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            throw new Exception("Invalid ID – refer to list and select a game ID");
        }

        GameData game = gameIDsMap.get(displayNumber);
        if (game == null) {
            throw new Exception("Invalid ID – refer to list and select a game ID");
        }

        server.observeGame(game.gameID());
        ChessBoardUI.drawBoard(game.game(), ChessGame.TeamColor.WHITE);
        return String.format("You are now observing game \"%s\".", game.gameName());
    }

    public String logout() throws Exception {
        assertSignedIn();
        state = State.SIGNEDOUT;
        server.logout();
        String previousUser = this.username;
        this.username = null;
        return String.format("\"%s\" successfully signed out. Thank you for playing.", previousUser);
    }

    public String redrawBoard() throws Exception {
        assertInGame();
        ChessGame.TeamColor perspective = joinedGame.blackUsername() == username 
          ? ChessGame.TeamColor.BLACK 
          : ChessGame.TeamColor.WHITE;
        ChessBoardUI.drawBoard(joinedGame.game(), perspective);
        return String.format("Board successfully redrawn");

    }

    public String highlightLegalMoves(String... params) throws Exception {
        assertInGame();
        int col;
        int row;
        ChessPosition piecePosition;
        ChessPiece highlightedPiece;
        if (params.length != 1) {
            throw new Exception("Expected: show_moves <pos> (ex. show_moves a1)");
        }
        col = params[0].charAt(0) - 'a' + 1;
        row = params[0].charAt(1) - '0';
        piecePosition = new ChessPosition(row, col);
        highlightedPiece = joinedGame.game().getBoard().getPiece(piecePosition);

        if (highlightedPiece == null) {
            throw new Exception(String.format("There is no piece at pos: %s", params[0]));
        }
        return String.format("Displaying all legal moves for %c at %d", highlightedPiece.toString(), params[0]);
    }

    public String makeMove(String... params) throws Exception {
        assertPlayer();
        return String.format("\"%s\" successfully signed out. Thank you for playing.");
    }

    public String leave() throws Exception {
        assertInGame();
        state = State.SIGNEDIN;
        return String.format("\"%s\" successfully signed out. Thank you for playing.");
    }

    public String resign() throws Exception {
        assertPlayer();
        state = State.SIGNEDIN;
        return String.format("\"%s\" successfully signed out. Thank you for playing.");
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
        else if (state == State.SIGNEDIN) {
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
        else if (state == State.PLAYER) {
            return """
                    redraw - redraws the chessboard
                    show_moves <pos> - highlights all legal moves for piece at pos (ex. a1)                    
                    move <start> <end> - move piece from start to end (ex. move a1 a2)
                    leave - exits game and allows other player to take your spot
                    resign - forfeit the game
                    help - with possible commands
                    """;
        }
        return """
                redraw - redraws the chessboard
                show_moves <pos> - highlights all legal moves for piece at pos (ex. a1)
                leave - exits game and returns to previous menu
                help - with possible commands
                """;
    }

    private void assertSignedIn() throws Exception {
        if (state != State.SIGNEDIN) {
            throw new Exception("You must be signed in to run that command");
        }
    }

    private void assertPlayer() throws Exception {
        if (state != State.PLAYER) {
            throw new Exception("You must be the player to run that command");
        }
    }

    private void assertInGame() throws Exception {
        if (state == State.SIGNEDOUT || state == State.SIGNEDIN) {
            throw new Exception("You must be in a game to run that command");
        }
    }

    public enum State {
        SIGNEDOUT,
        SIGNEDIN,
        PLAYER,
        OBSERVER
    }
}