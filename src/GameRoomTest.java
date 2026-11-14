import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Offline checks for GameRoom's timer and game-over behaviour.
 * Run: java -cp out GameRoomTest
 *
 * These tests call tick() directly, so they verify countdown state changes
 * without waiting ten real minutes or opening network sockets.
 */
public class GameRoomTest {

    private static int checks = 0;

    private static void check(boolean ok, String description) {
        checks++;
        if (!ok) {
            throw new AssertionError("FAIL: " + description);
        }
        System.out.println("PASS: " + description);
    }

    /** Captures all messages that GameRoom sends to one role. */
    static class TestPlayer {
        private final StringWriter messages = new StringWriter();
        final PrintWriter out = new PrintWriter(messages, true);

        String readAndClear() {
            out.flush();
            String result = messages.toString();
            messages.getBuffer().setLength(0);
            return result;
        }
    }

    private static GameRoom joinAll(TestPlayer inspector, TestPlayer analyst,
                                    TestPlayer operator) {
        GameRoom room = new GameRoom();
        room.join(Role.INSPECTOR, inspector.out);
        room.join(Role.ANALYST, analyst.out);
        room.join(Role.OPERATOR, operator.out);
        clear(inspector, analyst, operator);
        return room;
    }

    private static void clear(TestPlayer... players) {
        for (TestPlayer player : players) {
            player.readAndClear();
        }
    }

    private static int count(String text, String target) {
        int matches = 0;
        int from = 0;
        while ((from = text.indexOf(target, from)) >= 0) {
            matches++;
            from += target.length();
        }
        return matches;
    }

    private static void testTimerWaitsForAllPlayers() {
        System.out.println("\n--- TIMER waits for all players ---");
        GameRoom room = new GameRoom();
        TestPlayer inspector = new TestPlayer();
        TestPlayer analyst = new TestPlayer();
        TestPlayer operator = new TestPlayer();

        room.join(Role.INSPECTOR, inspector.out);
        room.join(Role.ANALYST, analyst.out);
        clear(inspector, analyst);

        for (int i = 0; i < 120; i++) room.tick();
        check(inspector.readAndClear().isEmpty(),
                "Timer does not run while only two players are connected");

        room.join(Role.OPERATOR, operator.out);
        clear(inspector, analyst, operator);
        for (int i = 0; i < 59; i++) room.tick();
        check(inspector.readAndClear().isEmpty(),
                "No minute notice appears before 60 active ticks");

        room.tick();
        check(inspector.readAndClear().contains("TIME LEFT: 540 s"),
                "Timer starts from 600 seconds when the third player joins");
    }

    private static void testDisconnectPausesTimer() {
        System.out.println("\n--- TIMER pauses and resumes ---");
        TestPlayer inspector = new TestPlayer();
        TestPlayer analyst = new TestPlayer();
        TestPlayer operator = new TestPlayer();
        GameRoom room = joinAll(inspector, analyst, operator);

        for (int i = 0; i < 59; i++) room.tick();
        room.leave(Role.OPERATOR);
        clear(inspector, analyst, operator);

        for (int i = 0; i < 100; i++) room.tick();
        check(inspector.readAndClear().isEmpty(),
                "Disconnected time does not reduce the countdown");

        room.join(Role.OPERATOR, operator.out);
        String rejoinMessages = inspector.readAndClear();
        check(rejoinMessages.contains("ALL PLAYERS IN - STAGE 1: POWER"),
                "Rejoin announces the current stage");
        clear(analyst, operator);

        room.tick();
        check(inspector.readAndClear().contains("TIME LEFT: 540 s"),
                "Timer resumes from the paused value after rejoin");
    }

    private static void testTimeoutAndGameOver() {
        System.out.println("\n--- TIMEOUT and game over ---");
        TestPlayer inspector = new TestPlayer();
        TestPlayer analyst = new TestPlayer();
        TestPlayer operator = new TestPlayer();
        GameRoom room = joinAll(inspector, analyst, operator);

        for (int i = 0; i < 590; i++) room.tick();
        String tenSecondMessages = inspector.readAndClear();
        check(tenSecondMessages.contains("TIME LEFT: 10 s"),
                "Final ten-second notices begin at 10 seconds");

        for (int i = 0; i < 9; i++) room.tick();
        check(inspector.readAndClear().contains("TIME LEFT: 1 s"),
                "Countdown reaches the final one-second notice");

        room.tick();
        String timeoutMessages = inspector.readAndClear();
        check(count(timeoutMessages, "TIME UP - FAILED") == 1,
                "Timeout failure is broadcast exactly once");

        for (int i = 0; i < 20; i++) room.tick();
        check(inspector.readAndClear().isEmpty(),
                "Further ticks do nothing after game over");

        room.handle(Role.INSPECTOR, "manual");
        check(inspector.readAndClear().contains("GAME OVER"),
                "Commands are rejected after timeout");

        room.leave(Role.OPERATOR);
        clear(inspector, analyst, operator);
        room.join(Role.OPERATOR, operator.out);
        check(inspector.readAndClear().contains("ALL PLAYERS IN - GAME OVER"),
                "Rejoining after timeout reports game over safely");
    }

    public static void main(String[] args) {
        testTimerWaitsForAllPlayers();
        testDisconnectPausesTimer();
        testTimeoutAndGameOver();
        System.out.println("\nALL GAME ROOM TESTS PASSED (" + checks + " checks)");
    }
}
