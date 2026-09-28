/**
 * CircuitBreak - Member 3
 * Voltage Divider Puzzle Logic
 *
 * Calculates the output voltage of a voltage divider and checks
 * whether the player's resistor choice solves the puzzle.
 */
public class VoltageDividerPuzzle {

    // Circuit constants
    private static final double VIN = 10.0;          // Input voltage (V)
    private static final double RTOP = 1000.0;       // Top resistor (ohms)
    private static final double TARGET_VOUT = 5.0;   // Required output voltage (V)

    // Resistor settings available to Player 3
    private static final double[] VALID_CHOICES = {
        500.0,
        1000.0,
        2000.0
    };

    /**
     * Calculates Vout using the voltage-divider formula:
     * Vout = Vin * Rbottom / (Rtop + Rbottom)
     */
    public static double calculateVout(double rBottom) {
        if (rBottom <= 0) {
            throw new IllegalArgumentException("Rbottom must be greater than 0.");
        }

        return VIN * rBottom / (RTOP + rBottom);
    }

    /**
     * Checks whether the entered resistor value is one of the allowed settings.
     */
    public static boolean isValidChoice(double rBottom) {
        for (double choice : VALID_CHOICES) {
            if (Double.compare(choice, rBottom) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the player's choice and returns the puzzle result.
     */
    public static String checkChoice(double rBottom) {
        if (!isValidChoice(rBottom)) {
            return "INVALID CHOICE";
        }

        double vout = calculateVout(rBottom);

        if (Math.abs(vout - TARGET_VOUT) < 0.01) {
            return "CORRECT - PUZZLE COMPLETED";
        }

        return "VALID CHOICE - NOT CORRECT";
    }

    public static double getTargetVoltage() {
        return TARGET_VOUT;
    }

    public static double getVin() {
        return VIN;
    }

    public static double getRtop() {
        return RTOP;
    }
}
