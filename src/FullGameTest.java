import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;

/**
 * Plays a whole game over real TCP sockets against a real CircuitBreakServer and prints the
 * Part 5 test table. Run:  java -cp out FullGameTest   (port 6600 must be free)
 * Not covered: test 9 (10-minute timeout) and test 10 (3 separate laptops).
 */
public class FullGameTest {
    static int failures = 0;

    /** One player's connection. until() skips lines until one contains the text, or times out. */
    static class P {
        final Socket s;
        final BufferedReader in;
        final PrintWriter out;

        P() throws IOException {
            s = new Socket("127.0.0.1", CircuitBreakServer.PORT);
            s.setSoTimeout(4000);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
        }

        void send(String line) { out.println(line); }

        String until(String needle) {
            try {
                String line;
                while ((line = in.readLine()) != null) if (line.contains(needle)) return line;
            } catch (IOException timeout) { }
            return null;
        }

        void close() throws IOException { s.close(); }
    }

    /** Runs the real server in its own thread so the test can connect to it. */
    static class ServerTask implements Runnable {
        public void run() {
            try {
                CircuitBreakServer.main(new String[0]);
            } catch (IOException e) {
                System.out.println("Server did not start: " + e.getMessage());
                System.exit(2);
            }
        }
    }

    static void row(int n, String test, String expected, String actual) {
        boolean ok = actual != null && actual.contains(expected);
        if (!ok) failures++;
        System.out.println(LocalDate.now() + " | " + n + " | " + test + " | " + expected + " | "
                + (ok ? "PASS" : "FAIL (got: " + actual + ")"));
    }

    static P join(String role) throws IOException {
        P p = new P();
        p.send("join " + role);
        p.until("WELCOME " + role);
        return p;
    }

    public static void main(String[] args) throws Exception {
        Thread server = new Thread(new ServerTask());
        server.setDaemon(true);
        server.start();
        Thread.sleep(500);

        System.out.println("date       | #  | test                                                 | expected                   | result");
        P ins = join("inspector"), ana = join("analyst"), op = join("operator");
        row(1, "three players join, all told ALL PLAYERS IN", "ALL PLAYERS IN", ins.until("ALL PLAYERS IN"));

        P fourth = new P();
        fourth.send("join analyst");
        row(2, "second player asks for a taken role (so a 4th is refused)", "ERROR: ANALYST is taken", fourth.until("ERROR"));
        fourth.close();

        ins.send("replace fuse");
        row(3, "Inspector types replace fuse", "only the Operator", ins.until("ERROR"));

        String fault = null;
        for (String part : new String[]{"fuse", "battery", "diode"}) {
            ana.send("measure " + part);
            String reply = ana.until(part.toUpperCase() + ":");
            if (reply != null && (reply.contains("OPEN") || reply.contains("-9.0") || reply.contains("no conduction"))) fault = part;
        }
        String right = fault == null ? "?" : fault.equals("fuse") ? "replace fuse" : "flip " + fault;
        String wrong = right.equals("replace fuse") ? "flip battery" : "replace fuse";
        row(4, "Analyst measurements identify exactly one fault", "fault found", fault == null ? "none" : "fault found");

        op.send(wrong);
        row(5, "wrong fix gives 30 s penalty to the operator", "30 s penalty", op.until("penalty"));
        row(5, "  ...and every player is told the clock dropped", "PENALTY: -30 s", ins.until("PENALTY"));

        op.send(right);
        row(4, "right fix moves everyone to the Lock stage", "STAGE 2: LOCK", ana.until("STAGE 2"));
        ins.until("STAGE 2");

        op.close();
        ana.send("lamp");
        row(8, "operator disconnects mid-game; analyst still served", "LAMP:", ana.until("LAMP:"));
        op = new P();
        op.send("join operator");
        row(8, "  ...and the operator can rejoin", "WELCOME operator", op.until("WELCOME"));

        op.send("set 000");
        op.until("SWITCHES");
        ins.send("arm");
        row(6, "lamp off + arm", "lamp is off", ins.until("lamp is off"));

        op.send("set 101");
        op.until("SWITCHES");
        ana.send("lamp");
        row(6, "set 101 turns the lamp on", "LAMP: ON", ana.until("LAMP:"));

        ins.send("arm");
        ins.until("ARM RECORDED");
        Thread.sleep(2000);
        op.send("arm");
        row(6, "arms 2 s apart are not enough", "ARM RECORDED", op.until("ARM"));

        ins.send("arm");
        row(6, "re-arm inside 1.5 s of the other opens the lock", "LOCK OPEN", ins.until("LOCK OPEN"));
        row(4, "lock opens: everyone moves to the Code stage", "STAGE 3: CODE", ana.until("STAGE 3"));

        ins.send("manual");
        String d1 = ins.until("DIGIT 1");
        ana.send("scan");
        String d2 = ana.until("DIGIT 2");
        op.send("panel");
        String d3 = op.until("DIGIT 3");
        row(11, "each role is given its own digit", "ok", d1 == null || d2 == null || d3 == null ? null : "ok");
        String code = d1.charAt(d1.indexOf("= ") + 2) + "" + d2.charAt(d2.indexOf("= ") + 2) + d3.charAt(d3.indexOf("= ") + 2);

        op.send("enter 000");
        row(5, "wrong vault code gives 30 s penalty", "30 s penalty", op.until("penalty"));
        op.send("enter " + code);
        row(10, "right code: everyone sees DOOR OPEN - ESCAPED", "DOOR OPEN - ESCAPED", ins.until("ESCAPED"));

        ana.send("lamp");
        row(10, "commands after the win are refused", "GAME OVER", ana.until("GAME OVER"));

        System.out.println(failures == 0 ? "\nALL PASSED" : "\n" + failures + " FAILED");
        System.exit(failures == 0 ? 0 : 1);
    }
}
