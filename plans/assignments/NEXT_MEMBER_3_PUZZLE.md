# Next task: Member 3 - first puzzle and safe shared state

**Target demo:** 2 October 2026. Read [NEXT_SHARED_CONTRACT.md](NEXT_SHARED_CONTRACT.md) together before coding. Your `VoltageDividerPuzzle.java` and `PuzzleTest.java` are already on `main`: demonstrate them first. The two-thread shared-state exercise is still to be demonstrated, so make it part of this task.

**Deliverable:** Keep your existing calculator and add a Java `GameRoom` state class around it. Start with 500-ohm bottom resistance. Validate Operator changes against the existing `{500, 1000, 2000}` choices, calculate integer millivolts, record completion only once, and return or make available the three different role views described in the shared contract. The current `checkChoice()` returns a result but stores no puzzle progress; the new class must own that progress. It must not open sockets or read the keyboard.

**Concurrency boundary:** Two server handler threads may call the puzzle at nearly the same time. Protect the shared resistor and completion state so each action observes a coherent value. Explain which operations must be atomic. Agree with Member 1 on the exact public method signatures before coding the integration; Member 1 will send your VIEW strings through the sockets.

**Proof:** Show outputs for all three resistor choices, reject an unsupported value, and show that two simultaneous attempts do not complete the puzzle twice. Then connect `GameRoom` to the real server and demonstrate the Operator changing the Analyst's reading. Submit source, exact run commands, and a short recording or live demonstration. Explain the voltage-divider equation, why 1000 ohms gives 5 V, and the race that synchronization prevents.

**Reference:** `basic thread program.zip` shows how to start several threads. Its separate thread-number fields are not an example of safely sharing mutable game data, so your puzzle must demonstrate that extra step.
