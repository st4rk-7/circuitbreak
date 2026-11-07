import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Temporary Week 1 test server for Member 2's client proof.
 * It sends a line every three seconds even when the client sends nothing.
 *
 * Run: java -cp out Member2StubServer [port]
 */
public class Member2StubServer {
    private static final int DEFAULT_PORT = 6601;

    public static void main(String[] args) throws IOException, InterruptedException {
        int port = args.length == 0 ? DEFAULT_PORT : Integer.parseInt(args[0]);

        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("Stub server listening on port " + port);
            try (Socket client = server.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                System.out.println("Client connected: " + client.getRemoteSocketAddress());
                out.println("Stub connected. Do not press Enter; periodic messages will still arrive.");

                for (int messageNumber = 1; messageNumber <= 20; messageNumber++) {
                    out.println("Periodic server message #" + messageNumber);
                    Thread.sleep(3000);

                    // ready() prevents the stub from blocking its periodic messages for keyboard input.
                    while (in.ready()) {
                        String message = in.readLine();
                        if (message == null || message.trim().equalsIgnoreCase("quit")) {
                            System.out.println("Client disconnected.");
                            return;
                        }
                        System.out.println("CLIENT> " + message);
                    }
                }
                out.println("Stub test complete. Closing connection.");
            }
        }
    }
}
