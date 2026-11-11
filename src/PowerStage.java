import java.util.Random;

/**
 * Stage 1: The Analyst measures components, the Inspector reads the manual,
 * and the Operator repairs the randomly selected fault.
 *
 * No sockets or console output belong in this class.
 */
public class PowerStage implements Stage {

    public enum Fault { FUSE, BATTERY, DIODE }

    private final Fault fault;
    private boolean solved;

    /** Real game: randomly choose one broken component. */
    public PowerStage() {
        this(Fault.values()[new Random().nextInt(Fault.values().length)]);
    }

    /** Test constructor: select a specific fault for repeatable tests. */
    public PowerStage(Fault fault) {
        if (fault == null) {
            throw new IllegalArgumentException("Fault cannot be null");
        }
        this.fault = fault;
        this.solved = false;
    }

    @Override
    public String name() {
        return "POWER";
    }

    @Override
    public synchronized boolean isSolved() {
        return solved;
    }

    @Override
    public synchronized String handle(Role who, String cmd) {
        if (who == null || cmd == null || cmd.trim().isEmpty()) {
            return "ERROR: invalid role or command";
        }

        String command = cmd.trim().toLowerCase();

        if (command.equals("manual")) {
            if (who != Role.INSPECTOR) {
                return "ERROR: only the Inspector can read the manual";
            }
            return "MANUAL: Fuse must show continuity. Battery must read +9 V. "
                    + "Diode must conduct at 0.7 V.";
        }

        if (command.startsWith("measure ")) {
            if (who != Role.ANALYST) {
                return "ERROR: only the Analyst can measure";
            }
            if (command.equals("measure fuse")) {
                if (fault == Fault.FUSE && !solved) return "FUSE: OPEN (no continuity)";
                return "FUSE: OK (continuity)";
            }
            if (command.equals("measure battery")) {
                if (fault == Fault.BATTERY && !solved) return "BATTERY: -9.0 V";
                return "BATTERY: +9.0 V";
            }
            if (command.equals("measure diode")) {
                if (fault == Fault.DIODE && !solved) return "DIODE: no conduction";
                return "DIODE: 0.7 V forward";
            }
            return "ERROR: unknown measurement";
        }

        if (command.equals("replace fuse")
                || command.equals("flip battery")
                || command.equals("flip diode")) {
            if (who != Role.OPERATOR) {
                return "ERROR: only the Operator can do that";
            }
            if (solved) {
                return "POWER: already fixed";
            }

            boolean correctRepair =
                    (fault == Fault.FUSE && command.equals("replace fuse"))
                    || (fault == Fault.BATTERY && command.equals("flip battery"))
                    || (fault == Fault.DIODE && command.equals("flip diode"));

            if (correctRepair) {
                solved = true;
                return "POWER FIXED";
            }

            // The server's GameRoom must apply the 30-second timer penalty
            // during integration. This offline stage only reports the event.
            return "nothing changed - 30 s penalty";
        }

        return "ERROR: unknown command";
    }
}
