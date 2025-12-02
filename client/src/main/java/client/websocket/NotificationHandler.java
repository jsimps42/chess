package client.websocket;

public interface NotificationHandler {
    void notify(WebSocketFacade.Notification notification);
}
