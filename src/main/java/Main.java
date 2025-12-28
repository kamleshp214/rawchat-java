import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.websocket.WsContext;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public class Main {
    private static final Set<WsContext> users = ConcurrentHashMap.newKeySet();
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.staticFiles.add("/public", Location.CLASSPATH);
        });
        int port = 8080;
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                port = Integer.parseInt(envPort);
            } catch (NumberFormatException e) { }
        }
        app.start(port);
        app.ws("/chat", ws -> {
            ws.onConnect(ctx -> {
                users.add(ctx);
                System.out.println("User connected. Total: " + users.size());
            });
            ws.onClose(ctx -> {
                users.remove(ctx);
                System.out.println("User disconnected. Total: " + users.size());
            });
            ws.onMessage(ctx -> {
                String message = ctx.message();
                for (WsContext user : users) {
                    if (user.session.isOpen() && user != ctx) {
                        user.send(message);
                    }
                }
            });
        });
    }
}