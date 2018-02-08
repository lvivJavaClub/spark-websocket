import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class ChatWebSocketHandler {

    @OnWebSocketConnect
    public void onConnect(Session user) {
        String userID = "User" + Chat.nextUserNumber++;
        Chat.userUsernameMap.put(user, userID);
        Chat.sendHistory(user);
        Chat.broadcastMessage("Server", "User " + userID + " joined the chat");
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String userId = Chat.userUsernameMap.get(session);
        Chat.userUsernameMap.remove(session);
        Chat.broadcastMessage("Server", userId + " left the chat");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        Chat.broadcastMessage(Chat.userUsernameMap.get(session), message);
    }
}
