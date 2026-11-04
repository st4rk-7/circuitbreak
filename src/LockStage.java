import java.util.Locale;

/**
 * Stage 2: (A XOR B) AND C controls the lamp.
 * When the lamp is on, Inspector and Operator must both ARM within 1.5 s.
 */
public class LockStage implements Stage {

    private static final long ARM_WINDOW_NANOS = 1_500_000_000L;

    private String switches = "000";
    private long inspectorArmAt;
    private long operatorArmAt;
    private boolean inspectorArmed;
    private boolean operatorArmed;
    private boolean solved = false;
    private int completionCount = 0;

    @Override
    public String name() {
        return "LOCK";
    }

    @Override
    public synchronized boolean isSolved() {
        return solved;
    }

    /** For the concurrency test: completion must be recorded only once. */
    public synchronized int getCompletionCount() {
        return completionCount;
    }

    private boolean lampOn() {
        boolean a = switches.charAt(0) == '1';
        boolean b = switches.charAt(1) == '1';
        boolean c = switches.charAt(2) == '1';
        return (a ^ b) && c;
    }

    @Override
    public synchronized String handle(Role who, String cmd) {
        if (who == null || cmd == null || cmd.trim().isEmpty()) {
            return "ERROR: invalid role or command";
        }

        String command = cmd.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");

        if (command.equals("manual")) {
            if (who != Role.INSPECTOR) {
                return "ERROR: only the Inspector can read the manual";
            }
            return "MANUAL: Lamp = (A XOR B) AND C. "
                    + "When the lamp is on, Inspector and Operator must ARM within 1.5 s.";
        }

        if (command.equals("lamp")) {
            if (who != Role.ANALYST) {
                return "ERROR: only the Analyst can read the lamp";
            }
            return lampOn() ? "LAMP: ON" : "LAMP: OFF";
        }

        if (command.startsWith("set ")) {
            if (who != Role.OPERATOR) {
                return "ERROR: only the Operator can do that";
            }
            if (solved) {
                return "LOCK: already solved";
            }
            String value = command.substring(4);
            if (!value.matches("[01]{3}")) {
                return "ERROR: use set followed by three binary digits, e.g. set 101";
            }
            switches = value;
            // Old ARM presses must not count after switch settings change.
            inspectorArmed = false;
            operatorArmed = false;
            return lampOn() ? "SWITCHES: " + value + " LAMP: ON"
                    : "SWITCHES: " + value + " LAMP: OFF";
        }

        if (command.equals("arm")) {
            if (who != Role.INSPECTOR && who != Role.OPERATOR) {
                return "ERROR: only Inspector and Operator can ARM";
            }
            if (solved) {
                return "LOCK: already solved";
            }
            if (!lampOn()) {
                return "ERROR: lamp is off; cannot ARM";
            }

            long now = System.nanoTime();
            if (who == Role.INSPECTOR) {
                inspectorArmAt = now;
                inspectorArmed = true;
            } else {
                operatorArmAt = now;
                operatorArmed = true;
            }

            // Both ARM presses must be present and recent enough.
            if (inspectorArmed && operatorArmed
                    && Math.abs(inspectorArmAt - operatorArmAt) <= ARM_WINDOW_NANOS) {
                solved = true;
                completionCount++;
                return "LOCK OPEN - STAGE SOLVED";
            }
            return "ARM RECORDED - waiting for other player";
        }

        return "ERROR: unknown command";
    }
}
