package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<Session, Connection> connections = new ConcurrentHashMap<>();

    public void add(Session session, int gameID, String authToken, String username, boolean isPlayer) {
        Connection connection = new Connection(session, gameID, authToken, username, isPlayer);
        connections.put(session, connection);
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session excludeSession, NotificationMessage notification) throws IOException {
        String msg = notification.toString();
        for (Session c : connections.values()) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
            }
        }
    }

    public static class Connection {
        public Session session;
        public int gameID;
        public String authToken;
        public String username;
        public boolean isPlayer;

        public Connection(
          Session session,
          int gameID,
          String authToken,
          String username,
          boolean isPlayer) {
            this.session = session;
            this.gameID = gameID;
            this.authToken = authToken;
            this.username = username;
            this.isPlayer = isPlayer;
          }
    }
}