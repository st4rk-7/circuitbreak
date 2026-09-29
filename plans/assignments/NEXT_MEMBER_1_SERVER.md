# Next task: Member 1 - server and roles

**Target demo:** 2 October 2026. Read [NEXT_SHARED_CONTRACT.md](NEXT_SHARED_CONTRACT.md) together before coding. First show that the 25 September three-client echo task works; if it does not, repair that first.

**Deliverable:** Extend the committed `circuitbreakserver.java` so three clients can claim unique Inspector, Analyst, and Operator roles and stay connected. The server checks the role on every command. It routes a valid `SET|RBOTTOM_OHMS|1000` to Member 3's puzzle logic and sends a fresh role-specific view to each player. A fourth client or duplicate active role must receive a clear error. Do not read replies from the server keyboard.

**Implementation boundary:** Own socket accept, client handlers, role-to-connection tracking, command parsing, and sends. Agree with Member 3 on the method the server calls to change the resistor and obtain views. Keep network I/O out of the puzzle class. If their puzzle class is not ready, temporarily use a small stub that returns the agreed VIEW lines; replace it before the integrated demo.

**Proof:** Start server and three clients. Show unique roles, an Inspector SET rejected with `ERROR|FORBIDDEN`, an Operator SET accepted, and the other two clients still working after one disconnects. Submit source, exact run commands, and a short screen recording or live demonstration. Explain `accept()`, why each handler needs its own thread, and why a client-supplied role cannot authorize a SET.

**Reference:** `Chat_Thread.zip` → `ChatServerT.java` and `Server_Thread.java`; compare their thread creation with your `circuitbreakserver.java`. In the workshop code, each handler creates a `Scanner(System.in)`; explain why several server threads should not compete for that one keyboard.
