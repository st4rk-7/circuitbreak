import java.util.Random;

/**
 * MEMBER 3 — Stage 1: the door has no power. One part is broken at random.
 * No networking here. Run `java StageTest` to check your work.
 */
public class PowerStage implements Stage {
    enum Fault { FUSE, BATTERY, DIODE }

    private final Fault fault;
    private boolean solved = false;

    public PowerStage() {
        this(Fault.values()[new Random().nextInt(3)]);
    }

    public PowerStage(Fault fault) { // tests use this to choose the fault
        this.fault = fault;
    }

    public String name() { return "POWER"; }

    public boolean isSolved() { return solved; }

    public String handle(Role who, String cmd) {
        // Example (done for you): the Inspector reads the manual.
        if (cmd.equals("manual")) {
            if (who != Role.INSPECTOR) return "ERROR: only the Inspector has the manual";
            return "MANUAL: Fuse must show continuity. Battery must read +9 V. Diode must conduct at 0.7 V.";
        }

        // Example (done for you): the Analyst measures the fuse.
        if (cmd.equals("measure fuse")) {
            if (who != Role.ANALYST) return "ERROR: only the Analyst can measure";
            return fault == Fault.FUSE ? "FUSE: OPEN (no continuity)" : "FUSE: OK (continuity)";
        }

        // TODO (Member 3): "measure battery"  -> "BATTERY: +9.0 V", or "BATTERY: -9.0 V" if the fault is BATTERY.
        //                  Only the Analyst may measure. Copy the fuse example above.

        // TODO (Member 3): "measure diode"    -> "DIODE: 0.7 V forward", or "DIODE: no conduction" if the fault is DIODE.

        // TODO (Member 3): "replace fuse", "flip battery", "flip diode"
        //   not the Operator   -> "ERROR: only the Operator can do that"
        //   the broken part    -> solved = true; return "FUSE fixed - POWER OK" (or BATTERY / DIODE)
        //   a healthy part     -> "nothing changed - 30 s penalty"
        //   Hint: decide which Fault the command is about, then compare it with `fault`.

        return "UNKNOWN COMMAND: " + cmd;
    }
}
