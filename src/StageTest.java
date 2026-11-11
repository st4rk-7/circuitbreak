/**
 * Run all Member 3 Week 1 checks without starting a server.
 * Run: java -cp out StageTest
 */
public class StageTest {

    private static int checks = 0;

    private static void check(boolean ok, String description) {
        checks++;
        if (!ok) {
            throw new AssertionError("FAIL: " + description);
        }
        System.out.println("PASS: " + description);
    }

    private static void testPowerFault(PowerStage.Fault fault, String badMeasurement, String repair) {
        PowerStage p = new PowerStage(fault);
        System.out.println("\n--- POWER fault: " + fault + " ---");
        check(p.name().equals("POWER"), "PowerStage name");
        check(p.handle(Role.INSPECTOR, "manual").contains("Fuse must show continuity"),
                "Inspector reads the repair manual");
        check(p.handle(Role.ANALYST, "measure fuse").contains("FUSE:"), "Analyst measures fuse");
        check(p.handle(Role.ANALYST, "measure battery").contains("BATTERY:"), "Analyst measures battery");
        check(p.handle(Role.ANALYST, "measure diode").contains("DIODE:"), "Analyst measures diode");
        String measurementCommand = fault == PowerStage.Fault.FUSE ? "measure fuse"
                : fault == PowerStage.Fault.BATTERY ? "measure battery" : "measure diode";
        check(p.handle(Role.ANALYST, measurementCommand).equals(badMeasurement),
                "Broken component gives the expected reading");
        check(p.handle(Role.INSPECTOR, repair).equals("ERROR: only the Operator can do that"),
                "Inspector cannot repair");
        check(p.handle(Role.ANALYST, repair).equals("ERROR: only the Operator can do that"),
                "Analyst cannot repair");
        check(!p.isSolved(), "Unauthorized repairs do not change state");
        String wrongRepair = fault == PowerStage.Fault.FUSE ? "flip battery" : "replace fuse";
        check(p.handle(Role.OPERATOR, wrongRepair).contains("30 s penalty"),
                "Wrong repair reports 30-second penalty");
        check(!p.isSolved(), "Wrong repair does not solve stage");
        check(p.handle(Role.OPERATOR, repair).equals("POWER FIXED"), "Correct repair works");
        check(p.isSolved(), "Power stage is solved");
    }

    private static void testLock() throws InterruptedException {
        System.out.println("\n--- LOCK rules ---");
        LockStage stage = new LockStage();
        check(stage.name().equals("LOCK"), "LockStage name");
        check(stage.handle(Role.ANALYST, "lamp").equals("LAMP: OFF"), "Initial 000 lamp off");
        check(stage.handle(Role.INSPECTOR, "arm").contains("lamp is off"),
                "ARM refused while lamp off");
        check(stage.handle(Role.ANALYST, "set 101").contains("only the Operator"),
                "Analyst cannot set switches");
        check(stage.handle(Role.INSPECTOR, "set 101").contains("only the Operator"),
                "Inspector cannot set switches");
        check(stage.handle(Role.OPERATOR, "set 111").endsWith("LAMP: OFF"),
                "111: (1 XOR 1) AND 1 = 0");
        check(stage.handle(Role.OPERATOR, "set 001").endsWith("LAMP: OFF"),
                "001: (0 XOR 0) AND 1 = 0");
        check(stage.handle(Role.OPERATOR, "set 101").endsWith("LAMP: ON"),
                "101: (1 XOR 0) AND 1 = 1");
        check(stage.handle(Role.OPERATOR, "set 011").endsWith("LAMP: ON"),
                "011: (0 XOR 1) AND 1 = 1");
        stage.handle(Role.OPERATOR, "set 101");
        check(stage.handle(Role.ANALYST, "lamp").equals("LAMP: ON"),
                "Analyst sees lamp on");
        check(stage.handle(Role.ANALYST, "arm").contains("only Inspector and Operator"),
                "Analyst cannot ARM");
        check(stage.handle(Role.OPERATOR, "set 102").startsWith("ERROR:"),
                "Invalid switch input rejected");
        check(stage.handle(Role.INSPECTOR, "manual").contains("XOR"),
                "Inspector sees logic-gate manual");

        // The team's specification explicitly requires arms 3 seconds apart to fail.
        check(stage.handle(Role.INSPECTOR, "arm").startsWith("ARM RECORDED"),
                "First ARM is recorded");
        Thread.sleep(3100);
        check(stage.handle(Role.OPERATOR, "arm").startsWith("ARM RECORDED"),
                "ARM 3 seconds later is too late");
        check(!stage.isSolved(), "Late ARM does not solve the lock");
        check(stage.handle(Role.INSPECTOR, "arm").equals("LOCK OPEN - STAGE SOLVED"),
                "Second pair inside 1.5 s solves lock");
        check(stage.isSolved() && stage.getCompletionCount() == 1,
                "Lock completion count is exactly one");
    }

    private static void testConcurrentArms() throws InterruptedException {
        System.out.println("\n--- 1000 two-thread ARM runs ---");
        for (int i = 0; i < 1000; i++) {
            LockStage stage = new LockStage();
            stage.handle(Role.OPERATOR, "set 101");
            ArmTask inspectorTask = new ArmTask(stage, Role.INSPECTOR);
            ArmTask operatorTask = new ArmTask(stage, Role.OPERATOR);
            Thread inspector = new Thread(inspectorTask);
            Thread operator = new Thread(operatorTask);
            go = false;
            inspector.start();
            operator.start();
            Thread.sleep(1); // let both threads reach the starting line
            go = true;       // both press ARM at (almost) the same moment
            inspector.join();
            operator.join();

            int solvedMessages = 0;
            if (inspectorTask.reply.equals("LOCK OPEN - STAGE SOLVED")) solvedMessages++;
            if (operatorTask.reply.equals("LOCK OPEN - STAGE SOLVED")) solvedMessages++;

            if (!stage.isSolved() || stage.getCompletionCount() != 1 || solvedMessages != 1) {
                throw new AssertionError("FAIL: concurrent ARM run " + (i + 1));
            }
        }
        check(true, "1000/1000 concurrent ARM runs solved exactly once");
    }

    // Both threads wait for this flag, then press ARM together.
    private static volatile boolean go = false;

    /** One player's ARM press, run in its own thread. */
    static class ArmTask implements Runnable {
        private final LockStage stage;
        private final Role role;
        String reply = "";

        ArmTask(LockStage stage, Role role) {
            this.stage = stage;
            this.role = role;
        }

        public void run() {
            while (!go) { } // wait for the starting signal
            reply = stage.handle(role, "arm");
        }
    }

    // Demonstrate role/permission checks for every published command.
    private static void printRoleCommandMatrix() {
        System.out.println("\n--- ROLE × COMMAND REPLIES (sample fault: FUSE) ---");
        String[] powerCommands = {
                "manual", "measure fuse", "measure battery", "measure diode",
                "replace fuse", "flip battery", "flip diode", "unknown"
        };
        String[] lockCommands = {
                "manual", "lamp", "set 101", "arm", "set 102", "unknown"
        };
        for (Role role : Role.values()) {
            for (String command : powerCommands) {
                PowerStage p = new PowerStage(PowerStage.Fault.FUSE);
                System.out.println("POWER " + role + " > " + command + " => "
                        + p.handle(role, command));
            }
            for (String command : lockCommands) {
                LockStage l = new LockStage();
                System.out.println("LOCK  " + role + " > " + command + " => "
                        + l.handle(role, command));
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        testPowerFault(PowerStage.Fault.FUSE, "FUSE: OPEN (no continuity)", "replace fuse");
        testPowerFault(PowerStage.Fault.BATTERY, "BATTERY: -9.0 V", "flip battery");
        testPowerFault(PowerStage.Fault.DIODE, "DIODE: no conduction", "flip diode");
        testLock();
        testConcurrentArms();
        printRoleCommandMatrix();
        System.out.println("\nALL TESTS PASSED (" + checks + " checks + 1000 concurrent trials)");
    }
}
