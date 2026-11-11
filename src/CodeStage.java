import java.util.Random;

/**
 * Stage 3: the vault code has three digits and each role holds exactly one of them.
 * Inspector: manual (digit 1). Analyst: scan (digit 2). Operator: panel (digit 3), then enter ddd.
 * A wrong code returns text containing "penalty"; GameRoom owns the clock and applies it.
 */
public class CodeStage implements Stage {

    private final int[] digits = new int[3];
    private boolean solved = false;

    public CodeStage() {
        Random random = new Random();
        for (int i = 0; i < 3; i++) digits[i] = random.nextInt(9) + 1; // 1 to 9
    }

    /** True for exactly three characters, each a digit. */
    private boolean isThreeDigits(String text) {
        if (text.length() != 3) return false;
        for (int i = 0; i < 3; i++) {
            if (!Character.isDigit(text.charAt(i))) return false;
        }
        return true;
    }

    @Override
    public String name() {
        return "CODE";
    }

    @Override
    public synchronized boolean isSolved() {
        return solved;
    }

    @Override
    public synchronized String handle(Role who, String cmd) {
        if (solved) return "CODE: already solved";

        if (cmd.equals("manual")) {
            if (who != Role.INSPECTOR) return "ERROR: only the Inspector can read the manual";
            return "DIGIT 1 = " + digits[0] + " (Analyst and Operator hold the other two; the Operator types: enter <d1><d2><d3>)";
        }
        if (cmd.equals("scan")) {
            if (who != Role.ANALYST) return "ERROR: only the Analyst can scan";
            return "DIGIT 2 = " + digits[1];
        }
        if (cmd.equals("panel")) {
            if (who != Role.OPERATOR) return "ERROR: only the Operator can read the panel";
            return "DIGIT 3 = " + digits[2];
        }
        if (cmd.startsWith("enter ")) {
            if (who != Role.OPERATOR) return "ERROR: only the Operator can do that";
            String attempt = cmd.substring(6).trim();
            if (!isThreeDigits(attempt)) return "ERROR: use enter followed by three digits, e.g. enter 123";
            if (attempt.equals("" + digits[0] + digits[1] + digits[2])) {
                solved = true;
                return "VAULT OPEN - STAGE SOLVED";
            }
            return "WRONG CODE - 30 s penalty";
        }
        return "ERROR: unknown command";
    }
}
