# Current team checkpoint: understand and run the Java basics

**Date:** 2 October 2026. **Language:** Java, as required by the lecturer. This is the active task sheet. The `NEXT_*` cards are for after this checkpoint.

We are not marking work complete just because code was uploaded. Each person must run it, point to the lines they wrote or own, and explain the result in their own words. If a member cannot explain a line, pair up and simplify that piece until they can.

| Person | Small deliverable for 2 October | Show it live | Explain in two minutes |
|---|---|---|---|
| Member 1: server/coordinator | Run the committed `circuitbreakserver.java`. Fix only a problem that blocks its basic three-client demo. | Connect three terminals; leave one idle; send from the other two; close one and show the others still work. | What `accept()` returns; why `new Thread(...).start()` lets the server accept another client; what `readLine() == null` means. |
| Member 2: client | A tiny Java client that connects to the existing server, prints the greeting, sends one line and prints the reply. Use `ChatClientT.java` as a reading example. | Start server and client from separate terminals. Show the greeting and one echo. | Which side creates `ServerSocket`; which side creates `Socket`; why `println` needs a newline/flush before `readLine()` can return. |
| Member 3: puzzle | Run and explain the committed `VoltageDividerPuzzle.java` and `PuzzleTest.java`. Correct any result they cannot explain. | Show 500, 1000, 2000 and an invalid 1500 ohms; identify which solves the puzzle. | `Vout = Vin * Rbottom / (Rtop + Rbottom)`; why 1000 ohms gives 5 V; why `checkChoice()` currently does not remember that a puzzle was solved. |

**Twenty-minute preparation call:** Member 1 draws one server and three clients. Member 2 points to the socket connection. Member 3 points to the voltage calculation. Each member traces what happens when the Operator eventually sends `1000`. The team agrees that the next shared step is to send that value to the server and call the puzzle logic.

**Definition of done:** source committed or handed over, exact run commands, a live run or recording, and the explanation above without reading. Mark any missing item as incomplete and keep that task active. Short help sessions are better than assigning more files to read.

**After this checkpoint:** Member 2 adds a receiver thread; Member 3 adds shared puzzle state; Member 1 adds role assignment and integrates the parts. Use the `NEXT_*` cards and their shared message contract. Aim for the first playable puzzle by **9 October**, then review the remaining schedule together.

**Workshop references:** `Chat_Thread.zip` contains a server with one thread per client and a turn-taking client. `basic thread program.zip` shows `Runnable` and `Thread.start()`. The archives are local course materials and are not on GitHub; teammates should obtain their own copies from class.
