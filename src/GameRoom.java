import java.io.PrintWriter;
import java.util.EnumMap;
import java.util.Map;

/**
 * MEMBER 1 — the shared game state. Every client thread calls into this one object,
 * so every public method is synchronized: only one thread changes the door at a time.
 */
public class GameRoom {
    private final Map<Role, PrintWriter> players = new EnumMap<>(Role.class);
    private final Stage[] stages = { new PowerStage(), new LockStage() };
    private int current = 0;
    private int secondsLeft = 600; // 10 minutes
    private boolean over = false;

    /** Returns null if the player joined, otherwise the error text to send back. */
    public synchronized String join(Role role, PrintWriter out) {
        // TODO (Member 1): if players already contains this role -> return "ERROR: " + role + " is taken"
        //                  (there are only 3 roles, so this also refuses a 4th player)
        players.put(role, out);
        broadcast(role.name().toLowerCase() + " joined (" + players.size() + "/3)");
        if (players.size() == 3) broadcast("ALL PLAYERS IN - STAGE 1: " + stages[0].name());
        return null;
    }

    public synchronized void leave(Role role) {
        players.remove(role);
        broadcast(role.name().toLowerCase() + " left the game");
    }

    public synchronized void handle(Role role, String cmd) {
        if (over) { send(role, "GAME OVER"); return; }
        Stage stage = stages[current];
        send(role, stage.handle(role, cmd));

        // TODO (Member 1): if the reply contains "penalty" -> secondsLeft -= 30 and tell everyone.
        // TODO (Member 1): if stage.isSolved():
        //     current++;
        //     if current == stages.length -> broadcast("DOOR OPEN - ESCAPED"); over = true;
        //     else                        -> broadcast("STAGE " + (current + 1) + ": " + stages[current].name());
    }

    /** Called once per second by the timer thread in CircuitBreakServer. */
    public synchronized void tick() {
        if (over || players.size() < 3) return; // clock runs only while all three are in
        // TODO (Member 1, week 2):
        //   secondsLeft--;
        //   every 60 s, and every second in the last 10 -> broadcast("TIME LEFT: " + secondsLeft + " s")
        //   at 0 -> broadcast("TIME UP - FAILED"); over = true;   (this must happen only once)
    }

    private void send(Role role, String msg) {
        PrintWriter out = players.get(role);
        if (out != null) out.println(msg);
    }

    private void broadcast(String msg) {
        System.out.println("[all] " + msg);
        // TODO (Member 1): send msg to every player. Loop over players.values() and println to each.
    }
}
