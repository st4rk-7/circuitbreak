# CircuitBreak: shared agreement for the next checkpoint

**Team meeting:** after the 2 October fundamentals checkpoint. **Working demonstration target:** 9 October 2026. These dates are revised targets from our project plan, not lecturer requirements.

Before coding, each member demonstrates and explains their task in [CURRENT_CHECKPOINT_2_OCT.md](CURRENT_CHECKPOINT_2_OCT.md). If a task is incomplete, finish it before taking on this contract. **Use Java:** the lecturer has specified it as the primary required language for this project.

## What must work on 9 October

Three clients remain connected at once. Each claims a different role. The Inspector sees the voltage-divider diagram values and target; the Analyst sees the current output voltage; the Operator sees allowed resistor settings and can change one. The server validates the change and sends updated role views. Selecting 1000 ohms for the bottom resistor solves the first puzzle. An Inspector trying to change a resistor receives an error. There is no timer or reconnect requirement for this checkpoint.

## Freeze this tiny text protocol together

All messages are UTF-8 text, one complete line per message, terminated with a newline. Do not send a Java object over the socket. The vertical bar separates fields. Send whole messages with `println` and automatic flushing, or an equivalent explicit flush. The server decides each client's role from its connection record; it must not trust a role named in an action.

| Sender | Example line | Meaning |
|---|---|---|
| Client | `JOIN|INSPECTOR` | Request an unused role; also allow `ANALYST` or `OPERATOR` |
| Server | `WELCOME|INSPECTOR` | Role is assigned to this connection |
| Server | `ERROR|ROLE_TAKEN` | Requested role is already active |
| Server | `ERROR|BAD_MESSAGE` | Command or field is invalid |
| Server | `ERROR|FORBIDDEN` | This role cannot operate the control |
| Client | `SET|RBOTTOM_OHMS|1000` | Operator selects the bottom resistor |
| Server to Inspector | `VIEW|VIN_MV=10000|RTOP_OHMS=1000|TARGET_MV=5000` | Hidden circuit information |
| Server to Analyst | `VIEW|VOUT_MV=3333` | Measurement before the action |
| Server to Operator | `VIEW|RBOTTOM_OHMS=500|OPTIONS_OHMS=500,1000,2000` | Current setting and available controls |
| Server | `SOLVED|1` | First puzzle completed |

After a valid SET, send a fresh VIEW to each connected role. A solved puzzle should report SOLVED exactly once. The example uses an ideal unloaded divider with `Vin = 10 V`, `Rtop = 1000 ohms`, and `Rbottom` in `{500, 1000, 2000}`. Compute `Vout = Vin * Rbottom / (Rtop + Rbottom)`, then round to integer millivolts for display. `Rbottom = 1000` produces exactly 5000 mV and solves the puzzle.

Keep this first contract small. It does not yet specify the final protocol. At the integration kickoff, all three members should read each example aloud and agree on exact capitalization and field names. If the contract changes, update this file first and tell the whole team. Do not independently alter message syntax.

## References and handoff

- `Chat_Thread.zip`: `ChatServerT.java` and `Server_Thread.java` show one handler thread per client. Its handlers each read `System.in`, which does not suit our game. Its loop also needs explicit handling when `readLine()` returns `null`.
- `basic thread program.zip`: `MultiThreadingRunnables.java` and `ThreadHandler.java` show `Runnable`, `new Thread(...)`, and `start()`; they do not show safe shared game state.
- The current `circuitbreakserver.java` already accepts several clients and echoes messages. The next change is role-aware puzzle behavior.

These workshop archives are in the coordinator's local project folder and are not tracked in Git. Teammates need copies from the course materials or a separate class-approved share.

## Checkpoint proof

Run three clients on localhost in separate terminals. Show distinct role views, one rejected unauthorized SET, one valid SET, and `SOLVED|1`. Close one client and show that the remaining two can still communicate. Each member explains their own code and traces the Operator's SET through the server to the three VIEW messages. Record actual results, including failures.
