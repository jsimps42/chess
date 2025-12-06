package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.*;
import websocket.commands.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import dataaccess.*;
import chess.*;


import java.io.IOException;

import javax.management.Notification;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        try {
            Gson gson = new Gson();
            UserGameCommand base = gson.fromJson(ctx.message(), UserGameCommand.class);
            switch (base.getCommandType()) {
                case CONNECT -> {
                    UserGameCommand action = gson.fromJson(ctx.message(), UserGameCommand.class);
                    connect(action, ctx.session);
                }

                case MAKE_MOVE -> {
                    MakeMoveCommand action = gson.fromJson(ctx.message(), MakeMoveCommand.class);
                    makeMove(action, ctx.session);
                }

                case LEAVE -> {
                    UserGameCommand action = gson.fromJson(ctx.message(), UserGameCommand.class);
                    leave(action, ctx.session);
                }

                case RESIGN -> {
                    UserGameCommand action = gson.fromJson(ctx.message(), UserGameCommand.class);
                    resign(action, ctx.session);
                }
            }
        } catch (IOException | DataAccessException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(UserGameCommand cmd, Session session) throws IOException, DataAccessException {
        AuthAccess authAccess = new MySQLAuthAccess();
        AuthData authData;
        try {
            authData = authAccess.getAuth(cmd.getAuthToken());
        } catch(Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        if (authData == null) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: invalid authToken")));
            return;
        }

        UserAccess userAccess = new MySQLUserAccess();
        UserData userData;
        try {
            userData = userAccess.getUser(authData.username());
        } catch(Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        if (userData == null) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: invalid authToken")));
            return;
        }

        GameAccess gameAccess = new MySQLGameAccess();
        GameData gameData;
        try { 
            gameData = gameAccess.getGame(cmd.getGameID());
        } catch(Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        if (gameData == null) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: invalid gameID")));
            return;
        }

        String username = userData.username();
        boolean isPlayer = username.equals(gameData.whiteUsername()) || username.equals(gameData.blackUsername());

        connections.add(session, cmd.getGameID(), cmd.getAuthToken(), username, isPlayer);
        session.getRemote().sendString(new Gson().toJson(new LoadGameMessage(gameData.game(), gameData.gameID())));

        String note;
        if (isPlayer) {
            String color = username.equals(gameData.whiteUsername()) ? "white" : "black";
            note = username + " joined as " + color;
        } else {
            note = username + " is observing the game";
        }

        connections.broadcast(session, new NotificationMessage(note), gameData.gameID());
    }

    private void leave(UserGameCommand cmd, Session session) throws IOException, DataAccessException {
        var connection = connections.get(session);
        if (connection == null) {
            return;
        }

        connections.remove(session);

        GameAccess gameAccess = new MySQLGameAccess();
        GameData gameData;
        try {
            gameData = gameAccess.getGame(connection.gameID);
        } catch(Exception e) {
            throw new DataAccessException(e.getMessage());
        }

        if (gameData != null) {
            if (connection.username.equals(gameData.whiteUsername())) {
                try {
                    gameAccess.updateGame(new GameData(
                      gameData.game(),
                      gameData.gameID(),
                      gameData.gameName(),
                      null,
                      gameData.blackUsername()));
                } catch(Exception e) {
                    throw new DataAccessException(e.getMessage());
                }
            }
            else if (connection.username.equals(gameData.blackUsername())) {
                try {
                    gameAccess.updateGame(new GameData(
                      gameData.game(),
                      gameData.gameID(),
                      gameData.gameName(),
                      gameData.whiteUsername(),
                      null));  
                } catch(Exception e) {
                    throw new DataAccessException(e.getMessage());
                }
            }
        }

        connections.broadcast(session, new NotificationMessage(connection.username + " left the game"), gameData.gameID());
    }

    private void resign(UserGameCommand cmd, Session session) throws DataAccessException, IOException {
        var info = connections.get(session);
        if (info == null) {
            return;
        }
        GameAccess gameAccess = new MySQLGameAccess();
        GameData gameData;
        try { 
            gameData = gameAccess.getGame(info.gameID);
        } catch(Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        ChessGame game = gameData.game();

        if (game.gameOver) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Game is already over.")));
            return;
        }

        boolean isPlayer = info.username.equals(gameData.whiteUsername()) || info.username.equals(gameData.blackUsername());
        if (!isPlayer) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("You are only an observer.")));
            return;
        }

        game.gameOver = true;
        try {
            gameAccess.updateGame(new GameData(
              game, 
              gameData.gameID(),
              gameData.gameName(),
              gameData.whiteUsername(),
             gameData.blackUsername()));
        } catch(Exception e) {
            throw new DataAccessException(e.getMessage());
        }

        connections.broadcastAll(new NotificationMessage(info.username + " has resigned and forfeited the game. Game Over."), gameData.gameID());
    }

    private void makeMove(MakeMoveCommand cmd, Session session) throws DataAccessException, IOException{
        var connection = connections.get(session);
        if (connection == null) {
            return;
        }

        AuthAccess authAccess = new MySQLAuthAccess();
        AuthData authData;
        try { 
            authData = authAccess.getAuth(cmd.getAuthToken());
        } catch(Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        if (authData == null) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: invalid authToken")));
            return;
        }

        GameAccess gameAccess = new MySQLGameAccess();
        GameData gameData;
        try {
            gameData = gameAccess.getGame(connection.gameID);
        } catch(Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        if (gameData == null) {
            return;
        }
        ChessGame game = gameData.game();

        if (game.gameOver) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("The game is over.")));
            return;
        }

        boolean isPlayer = connection.username.equals(gameData.whiteUsername()) 
          || connection.username.equals(gameData.blackUsername());
        if (!isPlayer) {
            try {
                session.getRemote().sendString(new Gson().toJson(new ErrorMessage("You are only an observer.")));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        ChessMove move = cmd.getMove();
        ChessGame.TeamColor playerColor = connection.username.equals(gameData.whiteUsername()) ? 
          ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

        if (game.getTeamTurn() != playerColor) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("It is not your turn")));
            return;
        }

        try {
            game.makeMove(move);
        } catch (InvalidMoveException ex) {
            ErrorMessage err = new ErrorMessage("Error: invalid move");
            session.getRemote().sendString(new Gson().toJson(err));
            return;
        }

        String start = move.getStartPosition().toString();
        String end = move.getEndPosition().toString();
        String notificationText = connection.username + " moved " + start + " to " + end;
        connections.broadcast(session, new NotificationMessage(notificationText), gameData.gameID());
        
        ChessGame.TeamColor otherTeam;
        String otherUsername; 
        if (connection.username.equals(gameData.whiteUsername())) {
            otherTeam = ChessGame.TeamColor.BLACK;
            otherUsername = gameData.blackUsername();
        }
        else {
            otherTeam = ChessGame.TeamColor.WHITE;
            otherUsername = gameData.whiteUsername();
        }

        if (game.isInCheck(otherTeam)) {
            if (game.isInCheckmate(otherTeam)) {
                connections.broadcastAll(
                  new NotificationMessage(connection.username + " has checkmated " + otherUsername + "."), 
                  gameData.gameID());
                game.gameOver = true;
            }
            else if (game.isInStalemate(otherTeam)) {
                connections.broadcastAll(
                  new NotificationMessage(connection.username + " and " + otherUsername + " are in a stalemate. Game over."), 
                  gameData.gameID());
                game.gameOver = true;
            }
            else {
                connections.broadcastAll(
                  new NotificationMessage(otherUsername + " is in check."), 
                  gameData.gameID());
                }
        }

        try {
            gameAccess.updateGame( new GameData(
              game, 
              gameData.gameID(), 
              gameData.gameName(), 
              gameData.whiteUsername(),
              gameData.blackUsername()));
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }

        connections.broadcastAll(new LoadGameMessage(game, gameData.gameID()), gameData.gameID());
    }
}