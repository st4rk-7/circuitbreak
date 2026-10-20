import java.util.concurrent.CountDownLatch;

/**
 * MEMBER 3 — run this after every change:  java -cp out StageTest
 * Your goal: turn every FAIL into PASS.
 */
public class StageTest {
    static int pass = 0, fail = 0;

    static void check(String name, String actual, String expected) {
        if (actual.contains(expected)) { pass++; System.out.println("PASS  " + name); }
        else { fail++; System.out.println("FAIL  " + name + "\n      expected: " + expected + "\n      got:      " + actual); }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("--- PowerStage ---");
        for (PowerStage.Fault f : PowerStage.Fault.values()) {
            PowerStage p = new PowerStage(f);
            check(f + ": fuse",    p.handle(Role.ANALYST, "measure fuse"),    f == PowerStage.Fault.FUSE ? "OPEN" : "OK");
            check(f + ": battery", p.handle(Role.ANALYST, "measure battery"), f == PowerStage.Fault.BATTERY ? "-9.0 V" : "+9.0 V");
            check(f + ": diode",   p.handle(Role.ANALYST, "measure diode"),   f == PowerStage.Fault.DIODE ? "no conduction" : "0.7 V forward");
        }
        PowerStage p = new PowerStage(PowerStage.Fault.DIODE);
        check("inspector cannot fix", p.handle(Role.INSPECTOR, "flip diode"), "ERROR");
        check("wrong fix = penalty",  p.handle(Role.OPERATOR, "replace fuse"), "penalty");
        check("still unsolved",       String.valueOf(p.isSolved()), "false");
        check("right fix",            p.handle(Role.OPERATOR, "flip diode"), "POWER OK");
        check("now solved",           String.valueOf(p.isSolved()), "true");

        System.out.println("--- LockStage ---");
        String[] truth = { "000", "001", "010", "011", "100", "101", "110", "111" };
        for (String bits : truth) {
            LockStage l = new LockStage();
            l.handle(Role.OPERATOR, "set " + bits);
            boolean y = ((bits.charAt(0) == '1') ^ (bits.charAt(1) == '1')) && bits.charAt(2) == '1';
            check("lamp " + bits, l.handle(Role.ANALYST, "lamp"), y ? "ON" : "OFF");
        }
        LockStage l = new LockStage();
        check("arm with lamp off", l.handle(Role.INSPECTOR, "arm"), "refused");
        l.handle(Role.OPERATOR, "set 101");
        check("analyst cannot arm", l.handle(Role.ANALYST, "arm"), "ERROR");
        check("first key",  l.handle(Role.INSPECTOR, "arm"), "waiting");
        check("second key", l.handle(Role.OPERATOR, "arm"), "ESCAPED");

        System.out.println("--- Two threads arm at the same moment, 1000 times ---");
        int good = 0;
        for (int run = 0; run < 1000; run++) {
            LockStage race = new LockStage();
            race.handle(Role.OPERATOR, "set 101");
            CountDownLatch go = new CountDownLatch(1);
            String[] replies = new String[2];
            Thread t1 = new Thread(() -> { await(go); replies[0] = race.handle(Role.INSPECTOR, "arm"); });
            Thread t2 = new Thread(() -> { await(go); replies[1] = race.handle(Role.OPERATOR, "arm"); });
            t1.start(); t2.start();
            go.countDown(); // release both threads together
            t1.join(); t2.join();
            int escaped = (replies[0].contains("ESCAPED") ? 1 : 0) + (replies[1].contains("ESCAPED") ? 1 : 0);
            if (escaped == 1 && race.isSolved()) good++;
        }
        check("solved exactly once in every run", good + "/1000", "1000/1000");

        System.out.println("\n" + pass + " passed, " + fail + " failed");
    }

    static void await(CountDownLatch l) {
        try { l.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
