# Next task: Member 2 - player client and role views

**Target demo:** 2 October 2026. Read [NEXT_SHARED_CONTRACT.md](NEXT_SHARED_CONTRACT.md) together before coding. First show that the 25 September client can receive a message without the user pressing Enter; if it cannot, finish that task first.

**Deliverable:** Make one Java terminal client that accepts server host, port, and a requested role. It sends `JOIN|ROLE`, shows the assigned role and the VIEW lines it receives, and lets the Operator select a bottom resistor. The Inspector and Analyst may attempt a SET so the team can demonstrate the server's rejection. Display `ERROR|...` and `SOLVED|1` clearly.

**Thread boundary:** Keep keyboard input on one thread and server reading on another. Do not wait for a reply after each typed command; updates can arrive at any time. Handle the server closing the connection so the receiver exits. The client displays the server's results; it must not decide whether the puzzle is solved.

**Proof:** Open three client terminals. Before anyone types, show that each receives a different VIEW. Type a resistor selection in the Operator terminal and show updated messages in the other terminals without pressing Enter there. Show a helpful connection error using a wrong host or port. Submit source, exact run commands, and a short screen recording or live demonstration. Explain why the workshop client's `readLine()`/keyboard loop cannot show unsolicited updates.

**Reference:** `Chat_Thread.zip` → `ChatClientT.java` is a useful socket example, but it waits for the server and then waits for the keyboard in one thread. `basic thread program.zip` → `MultiThreadingRunnables.java` and `ThreadHandler.java` show how to start the separate receiver thread.
