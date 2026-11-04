/**
 * MEMBER 3 — Stage 2: the lock opens when Y = (A XOR B) AND C is 1
 * and the Inspector and Operator both type "arm" within 1.5 seconds.
 */
public class LockStage implements Stage {
    private static final long WINDOW_NS = 1_500_000_000L; // 1.5 s in nanoseconds

    private boolean a, b, c;                 // the three switches, start at 000
    private long inspectorArmedAt = -1;      // System.nanoTime() of the last arm, -1 = never
    private long operatorArmedAt = -1;
    private boolean solved = false;

    public String name() { return "LOCK"; }

    public synchronized boolean isSolved() { return solved; }

    boolean lamp() {
        // TODO (Member 3): return (A XOR B) AND C.  In Java: ^ is XOR, && is AND.
        return false;
    }

    // synchronized: two players can type "arm" at the same moment, from two server threads.
    public synchronized String handle(Role who, String cmd) {
        if (cmd.equals("manual")) {
            if (who != Role.INSPECTOR) return "ERROR: only the Inspector has the manual";
            return "GATE: Y = (A XOR B) AND C. The lamp shows Y. "
                 + "The door unlocks when Y = 1 and both keys ARM within 1.5 s.";
        }

        // Example (done for you): "set 101" sets A=1, B=0, C=1.
        if (cmd.startsWith("set ")) {
            if (who != Role.OPERATOR) return "ERROR: only the Operator can do that";
            String bits = cmd.substring(4).trim();
            if (!bits.matches("[01]{3}")) return "ERROR: use three bits, e.g. set 101";
            a = bits.charAt(0) == '1';
            b = bits.charAt(1) == '1';
            c = bits.charAt(2) == '1';
            return "SWITCHES: A=" + bits.charAt(0) + " B=" + bits.charAt(1) + " C=" + bits.charAt(2);
        }

        if (cmd.equals("lamp")) {
            if (who != Role.ANALYST) return "ERROR: only the Analyst can see the lamp";
            return lamp() ? "LAMP: ON" : "LAMP: OFF";
        }

        if (cmd.equals("arm")) {
            // TODO (Member 3):
            // 1. Only INSPECTOR or OPERATOR may arm      -> else "ERROR: only the Inspector or Operator can arm"
            // 2. If the lamp is off                      -> "ARM refused: lamp is off"
            // 3. Save System.nanoTime() in inspectorArmedAt or operatorArmedAt
            // 4. If both are set and Math.abs(inspectorArmedAt - operatorArmedAt) <= WINDOW_NS
            //                                            -> solved = true; return "DOOR OPEN - ESCAPED"
            // 5. Otherwise                               -> "ARMED - waiting for the other key (1.5 s)"
            return "TODO: arm";
        }

        return "UNKNOWN COMMAND: " + cmd;
    }
}
