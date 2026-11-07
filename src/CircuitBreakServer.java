import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * MEMBER 1 — the server. Same shape as our old circuitbreakserver.java:
 * accept() a laptop, give it its own thread, repeat. LO-5 (sockets) + LO-6 (threads).
 */
public class CircuitBreakServer {
    public static final int PORT = 6600;

    public static void main(String[] args) throws IOException {
        GameRoom room = new GameRoom();

        // Timer thread: once a second it asks the room to count down.
        Thread timer = new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(1000);
                    room.tick();
                }
            } catch (InterruptedException e) {
                // server is stopping
            }
        });
        timer.setDaemon(true); // don't keep the JVM alive on its own
        timer.start();

        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("CircuitBreak server listening on port " + PORT);
            while (true) {
                Socket socket = server.accept(); // waits here until a laptop connects
                System.out.println("[+] connection from " + socket.getRemoteSocketAddress());
                new Thread(new ClientHandler(socket, room)).start(); // one thread per player
            }
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final GameRoom room;

        ClientHandler(Socket socket, GameRoom room) {
            this.socket = socket;
            this.room = room;
        }

        public void run() {
            Role role = null;
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

                out.println("CircuitBreak: type  join inspector | join analyst | join operator");
                String line;

                // 1. The first thing a player must do is join with a role.
                while (role == null && (line = in.readLine()) != null) {
                    Role wanted = parseJoin(line.trim().toLowerCase());
                    if (wanted == null) { out.println("First type: join inspector | join analyst | join operator"); continue; }
                    String error = room.join(wanted, out);
                    if (error != null) out.println(error);
                    else { role = wanted; out.println("WELCOME " + role.name().toLowerCase()); }
                }

                // 2. Every other line is a game command for this player's role.
                while (role != null && (line = in.readLine()) != null) {
                    line = line.trim().toLowerCase();
                    if (line.equals("quit")) break;
                    if (!line.isEmpty()) room.handle(role, line);
                }
            } catch (IOException e) {
                System.out.println("[!] " + e.getMessage());
            } finally {
                if (role != null) room.leave(role);
                try { socket.close(); } catch (IOException ignored) { }
                System.out.println("[-] disconnected " + (role == null ? "(no role)" : role));
            }
        }

        private static Role parseJoin(String line) {
            if (!line.startsWith("join ")) return null;
            try { return Role.valueOf(line.substring(5).trim().toUpperCase()); }
            catch (IllegalArgumentException e) { return null; }
        }
    }
}
