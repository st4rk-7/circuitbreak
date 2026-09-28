/**
 * Simple test program for the Member 3 VoltageDividerPuzzle class.
 */
public class PuzzleTest {

    public static void main(String[] args) {
        double[] testValues = {
            500.0,
            1000.0,
            2000.0,
            1500.0
        };

        for (double rBottom : testValues) {
            System.out.println("----------------------");
            System.out.println("Testing Rbottom = " + rBottom + " ohms");

            if (VoltageDividerPuzzle.isValidChoice(rBottom)) {
                double vout = VoltageDividerPuzzle.calculateVout(rBottom);
                System.out.printf("Vout = %.2f V%n", vout);
            } else {
                System.out.println("Vout = not calculated (invalid choice)");
            }

            System.out.println("Result: " + VoltageDividerPuzzle.checkChoice(rBottom));
        }

        System.out.println("----------------------");
    }
}
