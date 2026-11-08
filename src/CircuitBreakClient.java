import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MEMBER 2 — the program each player runs.
 *
 * Run: java -cp out CircuitBreakClient <server-host> <port>
 * Main thread: keyboard -> server. Receiver thread: server -> screen.
 */
public class CircuitBreakClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java -cp out CircuitBreakClient <server-host> <port>");
            System.out.println("Example: java -cp out CircuitBreakClient localhost 6600");
            return;
        }

        String host = args[0];
        int port;
        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port: " + args[1] + ". Use a number from 1 to 65535.");
            return;
        }
        if (port < 1 || port > 65535) {
            System.out.println("Invalid port: " + port + ". Use a number from 1 to 65535.");
            return;
        }

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("Connected to " + host + ":" + port);
            System.out.println("Type messages to send them. Type quit to disconnect.");

            AtomicBoolean clientIsClosing = new AtomicBoolean(false);
            Thread receiver = new Thread(() -> receiveMessages(in, clientIsClosing),
                    "server-message-receiver");
            receiver.start();

            // This main thread can wait for the keyboard without stopping the receiver thread.
            Scanner keyboard = new Scanner(System.in);
            while (keyboard.hasNextLine()) {
                String typed = keyboard.nextLine();
                out.println(typed);
                if (typed.trim().equalsIgnoreCase("quit")) break;
            }

            // Leaving this try block closes the socket and releases a receiver blocked in readLine().
            clientIsClosing.set(true);
            System.out.println("Disconnecting...");
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + host);
        } catch (ConnectException e) {
            System.out.println("No server at " + host + ":" + port
                    + ". Is it running? Are you using the correct network address?");
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }

    /** Runs only on the receiver thread and blocks until the server sends a full line. */
    private static void receiveMessages(BufferedReader in, AtomicBoolean clientIsClosing) {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("SERVER> " + line);
            }
            if (!clientIsClosing.get()) {
                System.out.println("Server closed the connection.");
                // The main thread may still be waiting for keyboard input, so end the process.
                System.exit(0);
            }
        } catch (IOException e) {
            if (!clientIsClosing.get()) {
                System.out.println("Connection lost: " + e.getMessage());
                // A blocked Scanner cannot be reliably interrupted by closing only the socket.
                System.exit(1);
            }
        }
    }
}
