import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * MEMBER 1 — the shared game state. Every client thread calls into this one object,
 * so every public method is synchronized: only one thread changes the door at a time.
 */
public class GameRoom {
    private final Map<Role, PrintWriter> players = new HashMap<Role, PrintWriter>();
    private final Stage[] stages = { new PowerStage(), new LockStage(), new CodeStage() };
    private int current = 0;
    private int secondsLeft = 600; // 10 minutes
    private boolean over = false;

    /** Returns null if the player joined, otherwise the error text to send back. */
    public synchronized String join(Role role, PrintWriter out) {
        if (players.containsKey(role)) return "ERROR: " + role + " is taken";
        players.put(role, out);
        broadcast(role.name().toLowerCase() + " joined (" + players.size() + "/3)");
        if (players.size() == 3) {
            if (over) {
                broadcast("ALL PLAYERS IN - GAME OVER");
            } else {
                broadcast("ALL PLAYERS IN - STAGE " + (current + 1) + ": " + stages[current].name());
            }
        }
        return null;
    }

    public synchronized void leave(Role role) {
        players.remove(role);
        broadcast(role.name().toLowerCase() + " left the game");
    }

    public synchronized void handle(Role role, String cmd) {
        if (over) { send(role, "GAME OVER"); return; }
        Stage stage = stages[current];
        String reply = stage.handle(role, cmd);
        send(role, reply);

        // The stage only reports a wrong fix; the clock belongs to the room, so the penalty is applied here.
        if (reply.contains("penalty")) {
            secondsLeft -= 30;
            broadcast("PENALTY: -30 s (" + secondsLeft + " s left)");
        }

        if (stage.isSolved()) {
            current++;
            if (current == stages.length) {
                broadcast("DOOR OPEN - ESCAPED");
                over = true;
            } else {
                broadcast("STAGE " + (current + 1) + ": " + stages[current].name());
            }
        }
    }

    /** Called once per second by the timer thread in CircuitBreakServer. */
    public synchronized void tick() {
        if (over || players.size() < 3) return; // clock runs only while all three are in
        secondsLeft--;
        // <= 0, not == 0: a 30 s penalty can jump the clock past zero. `over` makes FAILED fire once.
        if (secondsLeft <= 0) {
            broadcast("TIME UP - FAILED");
            over = true;
        } else if (secondsLeft % 60 == 0 || secondsLeft <= 10) {
            broadcast("TIME LEFT: " + secondsLeft + " s");
        }
    }

    private void send(Role role, String msg) {
        PrintWriter out = players.get(role);
        if (out != null) out.println(msg);
    }

    private void broadcast(String msg) {
        System.out.println("[all] " + msg);
        for (PrintWriter p : players.values()) {
            p.println(msg);
        }
    }
}
