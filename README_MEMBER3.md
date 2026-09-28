# CircuitBreak - Member 3 Contribution

## Role
Member 3 implements the puzzle logic for the player who changes the resistor setting.

## Files
- `VoltageDividerPuzzle.java` - contains the voltage-divider calculation, valid resistor settings, and puzzle checking logic.
- `PuzzleTest.java` - tests valid, correct, incorrect, and invalid resistor choices.

## Circuit used
- Input voltage (Vin) = 10 V
- Top resistor (Rtop) = 1000 ohms
- Player-controlled bottom resistor (Rbottom) choices:
  - 500 ohms
  - 1000 ohms
  - 2000 ohms
- Target output voltage = 5 V

## Formula
Vout = Vin x Rbottom / (Rtop + Rbottom)

## Expected test results
- 500 ohms -> 3.33 V -> VALID CHOICE - NOT CORRECT
- 1000 ohms -> 5.00 V -> CORRECT - PUZZLE COMPLETED
- 2000 ohms -> 6.67 V -> VALID CHOICE - NOT CORRECT
- 1500 ohms -> INVALID CHOICE

## Compile
```bash
javac VoltageDividerPuzzle.java PuzzleTest.java
```

## Run
```bash
java PuzzleTest
```

## Integration idea
The server can later call `VoltageDividerPuzzle.checkChoice(rBottom)` when Player 3 sends a resistor setting. The returned result can then be sent to the other players through the socket connection.
