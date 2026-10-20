import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * MEMBER 2 — the program each player runs:  java -cp out CircuitBreakClient <server-ip>
 * Main thread: keyboard -> server.   Extra thread (your TODO): server -> screen.
 */
public class CircuitBreakClient {
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : "localhost";

        try (Socket socket = new Socket(host, 6600);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             Scanner keyboard = new Scanner(System.in)) {

            System.out.println("Connected to " + host + ":6600");

            // TODO (Member 2): start ONE extra thread that listens to the server, forever:
            //     String line;
            //     while ((line = in.readLine()) != null) System.out.println(line);
            //     then print "Server closed the connection" and System.exit(0);
            //   readLine() throws IOException, so wrap the loop in try/catch.
            //   Why a thread? readLine() WAITS for the server. If the main thread waited there,
            //   the player could never type. Until you do this, you will see no replies at all.

            while (keyboard.hasNextLine()) {          // main thread: send what the player types
                String typed = keyboard.nextLine();
                out.println(typed);
                if (typed.trim().equalsIgnoreCase("quit")) break;
            }
        } catch (UnknownHostException e) {
            System.out.println("Unknown host: " + host);
        } catch (ConnectException e) {
            System.out.println("No server at " + host + ":6600. Is it running? Are you on the same Wi-Fi?");
        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
        }
    }
}
