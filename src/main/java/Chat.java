import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

import static j2html.TagCreator.*;
import static spark.Spark.*;

public class Chat {
    // this map is shared between sessions and threads, so it needs to be thread-safe (http://stackoverflow.com/a/2688817)
    static Map<Session, String> userUsernameMap = new ConcurrentHashMap<>();
    static Queue<String> history = new CircularFifoQueue<>(3);
    static int nextUserNumber = 1; //Assign to username for next connecting user

    public static void main(String[] args) {
        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        webSocket("/chat", ChatWebSocketHandler.class);
        init();
    }

    //Sends a message from one user to all users, along with a list of current usernames
    static void broadcastMessage(String sender, String message) {
        String messageString = String.valueOf(new JSONObject()
                .put("userMessage", createHtmlMessageFromSender(sender, message))
                .put("userlist", userUsernameMap.values()));

        userUsernameMap.keySet().stream().filter(Session::isOpen).forEach(session -> sendSafe(session, messageString));

        history.add(messageString);
    }

    //Builds a HTML element with a sender-name, a message, and a timestamp,
    private static String createHtmlMessageFromSender(String sender, String message) {
        return article(
                b(sender + " says:"),
                span(attrs(".timestamp"), new SimpleDateFormat("HH:mm:ss").format(new Date())),
                p(message)
        ).render();
    }

    static void sendHistory(Session user) {
        history.forEach(message -> sendSafe(user, message));
    }

    private static void sendSafe(Session user, String message) {
        try {
            user.getRemote().sendString(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
