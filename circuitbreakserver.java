import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CircuitBreakServer
 * 
 * Multi-threaded TCP server supporting concurrent client connections.
 * Maps to LO-5 (Socket Programming) and LO-6 (Concurrency with Threads).
 */
public class circuitbreakserver {
    public static final int PORT = 6600;
    private static final AtomicInteger clientCounter = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   CircuitBreak Server - Multi-Client Listener    ");
        System.out.println("==================================================");
        System.out.println("Listening on port: " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // Blocks until a new client connects (TCP 3-way handshake completes)
                Socket clientSocket = serverSocket.accept();
                int clientId = clientCounter.incrementAndGet();

                System.out.println("[+] Client " + clientId + " connected from: " 
                                   + clientSocket.getRemoteSocketAddress());

                // Spawn a new independent thread for this client connection
                ClientHandler handler = new ClientHandler(clientSocket, clientId);
                Thread clientThread = new Thread(handler);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("[-] Server encountered an error: " + e.getMessage());
        }
    }

    /**
     * Handles communication with a single connected client in a dedicated thread.
     */
    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final int clientId;

        public ClientHandler(Socket socket, int clientId) {
            this.socket = socket;
            this.clientId = clientId;
        }

        @Override
        public void run() {
            try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                // Send initial greeting to the connected client
                out.println("Connected to CircuitBreak Server. You are Client #" + clientId);

                String message;
                // Continuously read lines from this client until they disconnect
                while ((message = in.readLine()) != null) {
                    if (message.equalsIgnoreCase("exit") || message.equalsIgnoreCase("quit")) {
                        out.println("Goodbye Client #" + clientId + "!");
                        break;
                    }

                    System.out.println("[Client #" + clientId + "]: " + message);
                    out.println("Echo [Client #" + clientId + "]: " + message);
                }
            } catch (IOException e) {
                System.out.println("[!] Connection issue with Client #" + clientId + ": " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket for Client #" + clientId + ": " + e.getMessage());
                }
                System.out.println("[-] Client #" + clientId + " disconnected.");
            }
        }
    }
}

