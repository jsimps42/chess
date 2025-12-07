package client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Collection;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.ChessPiece;
import exception.ResponseException;
import model.*;
import server.ServerFacade;
import ui.ChessBoardUI;
import client.websocket.*;
import websocket.messages.*;
import static ui.EscapeSequences.*;

public class ChessClient implements NotificationHandler {
    private String username = null;
    private String authToken = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private final HashMap<Integer, Integer> gameIDsMap = new HashMap<>();
    private GameData joinedGameData = null;
    private final WebSocketFacade ws;
    private ChessGame.TeamColor playerColor = null;

    public ChessClient(String serverUrl) throws Exception {
        server = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, (NotificationHandler) this);
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
                case "highlight" -> highlightLegalMoves(params);
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
            authToken = server.register(username, password, email).authToken();
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
            authToken = server.login(username, password).authToken();
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
            GameData gameData = new GameData(new ChessGame(), 0, gameName, null, null);
            server.createGame(gameData);
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
            GameData gameData = games.get(i);
            int displayNumber = i + 1;

            gameIDsMap.put(displayNumber, gameData.gameID());

            String white = gameData.whiteUsername() != null ? gameData.whiteUsername() : "(empty)";
            String black = gameData.blackUsername() != null ? gameData.blackUsername() : "(empty)";

            System.out.printf("%s%d.%s \"%s\" — White: %s%s%s, Black: %s%s%s%n",
              SET_TEXT_BOLD, displayNumber, RESET,
              gameData.gameName(),
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
            throw new Exception("Invalid game ID – refer to list and select a game ID");
        }

        GameData gameData = server.getGame(gameIDsMap.get(displayNumber));
        if (gameData == null) {
            throw new Exception("Invalid game ID – use 'list' to see available games");
        }

        String requestedColor = params.length == 2 ? params[1].toUpperCase() : null;

        String white = gameData.whiteUsername();
        String black = gameData.blackUsername();
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
                server.joinGame(gameData.gameID(), chosenColor);
            } catch (ResponseException e) {
                if (e.getMessage().contains("already taken") && alreadyInGame) {
                } else {
                    throw e;
                }
            }
        }
        ChessGame game = gameData.game();
        if (game == null) {
            game = new ChessGame();
        }
        ChessBoardUI.drawBoard(game, perspective, null);
        joinedGameData = gameData;
        state = State.PLAYER;
        playerColor = perspective;
        ws.joinGame(authToken, gameData.gameID());
        if (chosenColor != null) {
            if (alreadyInGame) {
                return String.format("Rejoined game \"%s\" as %s", gameData.gameName(), chosenColor);
            } else {
                return String.format("Joined game \"%s\" as %s", gameData.gameName(), chosenColor);
            }
        } else {
            return String.format("Now observing game \"%s\"", gameData.gameName());
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
            throw new Exception("Invalid game ID – refer to list and select a game ID");
        }

        GameData gameData = server.getGame(gameIDsMap.get(displayNumber));
        if (gameData == null) {
            throw new Exception("Invalid game ID – refer to list and select a game ID");
        }

        ChessGame game = gameData.game();
        if (game == null) {
            game = new ChessGame();
        }
        ChessBoardUI.drawBoard(game, ChessGame.TeamColor.WHITE, null);
        joinedGameData = gameData;
        ws.joinGame(authToken, gameData.gameID());
        state = State.OBSERVER;
        return String.format("You are now observing game \"%s\".", gameData.gameName());
    }

    public String logout() throws Exception {
        assertSignedIn();
        state = State.SIGNEDOUT;
        server.logout();
        String previousUser = this.username;
        this.username = null;
        authToken = null;
        return String.format("\"%s\" successfully signed out. Thank you for playing.", previousUser);
    }

    public String redrawBoard() throws Exception {
        assertInGame();
        joinedGameData = server.getGame(joinedGameData.gameID());
        ChessBoardUI.drawBoard(joinedGameData.game(), playerColor, null);
        return String.format("Board successfully redrawn");
    }

    public String highlightLegalMoves(String... params) throws Exception {
        assertInGame();
        ChessPosition piecePosition;
        ChessPiece highlightedPiece;
        joinedGameData = server.getGame(joinedGameData.gameID());
        if (params.length != 1) {
            throw new Exception("Expected: highlight <pos> (ex. highlight a1)");
        }
        
        piecePosition = getPosition(params[0]);
        highlightedPiece = joinedGameData.game().getBoard().getPiece(piecePosition);

        if (highlightedPiece == null) {
            throw new Exception(String.format("There is no piece at pos: %s", params[0]));
        }
        ChessGame.TeamColor perspective = joinedGameData.blackUsername() == username 
          ? ChessGame.TeamColor.BLACK 
          : ChessGame.TeamColor.WHITE;
        Collection<ChessMove> legalMoves = joinedGameData.game().validMoves(piecePosition);
        ChessBoardUI.drawBoard(joinedGameData.game(), perspective, legalMoves);
        return String.format("Displaying all legal moves for piece at %s", params[0]);
    }

    public String makeMove(String... params) throws Exception {
        assertPlayer();
        joinedGameData = server.getGame(joinedGameData.gameID());

        if (params.length != 2) {
            throw new Exception("Expected: makeMove <start> <end> (ex. move a1 b2)");
        }

        ChessPosition start = getPosition(params[0]);
        ChessPosition end = getPosition(params[1]);
        ChessPiece.PieceType promotionPiece = null;

        if (checkNeedsPromotion(start, end)) {
            Scanner scanner = new Scanner(System.in);
            String choice = "";
            while (choice != "QUEEN" && choice != "BISHOP" && choice != "KNIGHT" && choice != "ROOK") {
                System.out.print("\nChoose your promotion piece (queen, bishop, knight, rook): ");
                choice = scanner.nextLine().toUpperCase();
            }
            if (choice == "QUEEN") {
                promotionPiece = ChessPiece.PieceType.QUEEN;
            }
            else if (choice == "BISHOP") {
                promotionPiece = ChessPiece.PieceType.BISHOP;
            }
            else if (choice == "KNIGHT") {
                promotionPiece = ChessPiece.PieceType.KNIGHT;
            }
            else {
                promotionPiece = ChessPiece.PieceType.ROOK;
            }
        }
        ChessMove attemptedMove = new ChessMove(start, end, promotionPiece);
        Collection<ChessMove> legalMoves = joinedGameData.game().validMoves(start);
        if (!legalMoves.contains(attemptedMove)) {
            throw new Exception ("Error: move is invalid. Try highlight <pos> to see legal moves.");
        }

        ws.makeMove(authToken, joinedGameData.gameID(), attemptedMove);
        return String.format("Move is being sent. Please wait...");
    }

    public String leave() throws Exception {
        assertInGame();
        joinedGameData = server.getGame(joinedGameData.gameID());
        ws.leave(authToken, joinedGameData.gameID());
        state = State.SIGNEDIN;
        joinedGameData = null;
        playerColor = null;
        return String.format("Successfully left the game.");
    }

    public String resign() throws Exception {
        assertPlayer();
        joinedGameData = server.getGame(joinedGameData.gameID());
         try {
            System.out.print("This will end the current game.\nType 'resign' again to confirm your resignation: ");
            Scanner scanner = new Scanner(System.in);
            String confirmation = scanner.nextLine().toLowerCase();
            if (confirmation != "resign") {
                return "Resignation cancelled. Continuing game.";
            }
            ws.resign(authToken, joinedGameData.gameID());
            state = State.SIGNEDIN;
            joinedGameData = null;
            return "Successfully resigned from the game.";
        } catch (Exception e) {
            return "Unable to resign.";
        }
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
                    highlight <pos> - highlights all legal moves for piece at pos (ex. a1)                    
                    move <start> <end> - move piece from start to end (ex. move a1 a2)
                    leave - exits game and allows other player to take your spot
                    resign - forfeit the game
                    help - with possible commands
                    """;
        }
        return """
                redraw - redraws the chessboard
                highlight <pos> - highlights all legal moves for piece at pos (ex. a1)
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

    private ChessPosition getPosition(String param) throws Exception {
        int col;
        int row;

        if (param.length() != 2) {
            throw new Exception("Error: position is invalid. Should be between a1 and h8.");
        }

        col = param.charAt(0) - 'a' + 1;
        row = param.charAt(1) - '0';

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new Exception("Error: position is out of bounds. Should be between a1 and h8.");
        }
        return new ChessPosition(row, col);
    }

    private boolean checkNeedsPromotion(ChessPosition start, ChessPosition end) {
        ChessPiece movingPiece = joinedGameData.game().getBoard().getPiece(start);
        if (movingPiece.getPieceType() == ChessPiece.PieceType.PAWN &&
          (end.getRow() == 1 || end.getRow() == 8)) {
            return true;
        }
        return false;
    }

    public enum State {
        SIGNEDOUT,
        SIGNEDIN,
        PLAYER,
        OBSERVER
    }

    @Override
    public void notify(ServerMessage notification) {
        if (notification.getServerMessageType() == ServerMessage.ServerMessageType.LOAD_GAME) {
            LoadGameMessage loadGameMessage = (LoadGameMessage) notification;
            joinedGameData = new GameData(
              loadGameMessage.getGame(),
              joinedGameData.gameID(),
              joinedGameData.gameName(),
              joinedGameData.whiteUsername(),
              joinedGameData.blackUsername());
            try { 
                redrawBoard();
            } catch (Exception e) {
            }
        }

        else if (notification.getServerMessageType() == ServerMessage.ServerMessageType.ERROR) {
            ErrorMessage errorMessage = (ErrorMessage) notification;
            System.out.println(errorMessage.getErrorMessage());
        }

        else if (notification.getServerMessageType() == ServerMessage.ServerMessageType.NOTIFICATION) {
            NotificationMessage notificationMessage = (NotificationMessage) notification;
            System.out.println(notificationMessage.getNotificationMessage());
        }
    }
}