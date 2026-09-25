# CircuitBreak: three-person delivery and viva plan

Prepared 23 September 2026. Deadline from the supplied project brief: 14 November 2026. This is a proposed team plan, not a lecturer-issued marking rubric. Member 1 is assumed to be the coordinator; substitute real names before sharing the task cards.

## 1. What we are building

Three people on separate computers repair a virtual circuit together. The Inspector sees the diagram and target, the Analyst sees measurements, and the Operator changes settings. A Java server owns the game and sends different information to each player. Players speak to each other in person; voice chat is unnecessary.

Build one server, one room, exactly three roles, three short puzzles, a shared timer, and basic disconnect/rejoin recovery while the server stays running. Use Java TCP sockets and a terminal interface as the working choice because the first-session examples already use Java sockets. Ask the lecturer to confirm permitted libraries/language and assessment expectations before locking this choice. Do not assume GUI, UDP, hardware, or a database is compulsory.

Defer GUI, accounts, multiple rooms, online deployment, hardware, and realistic waveform simulation. The game is a software simulation, not an embedded implementation. Finish a complete first puzzle before adding the other two.

## 2. What the session code already teaches

Source directory: `/home/st4rk/Areas/Academics/exercises/JAVA`.

| Existing file | Lesson | Needed extension |
|---|---|---|
| myserver.java / myclient.java | TCP accept, connect, send, read, close | Keep sessions open and serve multiple players |
| serverchat.java | Two-way line-based messages | Move each accepted connection into a handler thread; remove server keyboard replies |
| clientchat.java | Send text and receive a reply | Receive server updates in a background thread while keyboard input stays active |
| udpserver.java / udpclient.java | Datagram send/receive | Use for TCP-versus-UDP understanding; not required in the game |

The chat server has an accept loop, but the conversation happens inside it before the next accept. That is sequential service, not concurrent client handling. The chat client is turn-taking, so it cannot reliably show unsolicited updates while waiting at the keyboard. These are the two main changes to understand first.

Inspection only: no course files were changed. Compilation was attempted, but `javac` was not available on this task's PATH. Confirm a working JDK on all three machines at the first checkpoint.

## 3. Ownership and LO evidence

LO-5: Design and implement network applications using socket programming.
LO-6: Create client-server applications using concurrent programming with threads.

| Person | Owns | LO-5 evidence | LO-6 evidence |
|---|---|---|---|
| Member 1: coordinator/server | ServerMain, ClientHandler, RoomSessions, shared Protocol; integration coordination | Accept sockets, decode requests, deliver role-specific responses, recover connections | Three live handlers; synchronized access to session membership and per-client sends |
| Member 2: client | ClientMain, ClientConnection, ConsoleView; client error handling | Connect using host/port, encode actions, read and display server messages | Receiver thread continues while keyboard input waits; orderly shutdown |
| Member 3: game/state | GameRoom, GameState, three Puzzle implementations; scripted test clients | Validate received actions and construct role-specific network views; write a small socket test driver | Atomic shared-state transitions; timer and player commands use the same state protection |

Ownership means implementation, tests, explanation, and fixing defects in that part. Member 1 does not silently complete other members' work. Everyone learns the complete connection-to-update path; contribution boundaries do not limit viva questions.

## 4. Agree these interfaces first

Proposed files and class names are a blueprint, not existing implementation. Use one repository and one shared Protocol definition. Do not independently invent message formats.

- ClientHandler parses input and attaches the role from the server's session record. Never trust a role supplied in an action message.
- GameRoom.apply(session, command) validates and updates state atomically, returning role-specific immutable views with an increasing state version.
- GameRoom.snapshot(role) returns only information allowed for that role.
- ClientConnection.send(command) sends a complete framed message; its receiver publishes decoded events to ConsoleView.
- Game logic does not open sockets or read terminal input. The view does not decide whether a puzzle was solved.

Start with UTF-8, one message per newline, pipe-separated fields, fixed command names, and numeric arguments. Reject unsupported commands, extra fields, invalid numbers, embedded delimiters, and oversized messages. Set and document an input limit, for example 1024 characters, in the reader itself. Plain unbounded readLine() plus a later length check does not enforce a memory bound. TCP is a byte stream: one write is not guaranteed to equal one read. A complete newline-delimited line defines one message.

Example conversation (illustrative; complete the grammar together on 24 September):

    JOIN|INSPECTOR
    WELCOME|INSPECTOR|<rejoin-token>
    READY
    SET|R_BOTTOM|1000
    VIEW|17|ANALYST|VOLTAGE_MV|3000
    ERROR|ROLE_NOT_ALLOWED
    REJOIN|<rejoin-token>

The server, not the client, assigns session ownership. The Operator may SET; other roles cannot. Deny duplicate active roles and a fourth player. Keep a disconnected role reserved for its rejoin token. Reject reuse while that role is actively connected. Tokens are only a simple local-demo reconnect mechanism, not a claim of production authentication.

## 5. Threading rules the team must explain

Server: an accept thread, one handler per connected client, and one timer task. The timer uses elapsed time, not a counter that assumes every sleep is exact. Client: main keyboard thread plus a network receiver thread.

Use one documented lock/synchronized boundary for game state, including timer updates, readiness, actions, puzzle transitions and pause/resume. Keep socket writes outside that game-state lock. Serialize whole outgoing messages per client so concurrent senders cannot mix their output. Attach monotonically increasing versions to game snapshots, and let clients discard older snapshots if delivery races reorder them. A role view is a complete snapshot, not an incremental patch requiring every prior version.

Never hold the session-registry lock while acquiring the game lock, or vice versa: copy the needed session references and release first. Close dead connections and remove/replace them consistently. Do not mutate published snapshots. A slow client must not hold the game-state lock and freeze everyone.

Start the timer only once all roles are connected and ready. Pause when a required role is detected disconnected. Resume only when all three return and are ready. Include periodic PING/PONG and a documented timeout so unplugging a network cable is detected; readLine() returning null alone covers orderly closure but not all network failures. For example, begin with a 2-second heartbeat and a 10-second timeout, then verify on the actual LAN. These are proposed settings, not guaranteed timing.

Reconnect restores the same role and current view while the server lives. Server restart recovery is outside scope. Explain this limitation honestly.

## 6. Puzzles that are easy to implement and defend

1. Voltage divider: Inspector sees source voltage, R-top, and target; Analyst sees measured V-out; Operator selects R-bottom. Use V-out = V-in * R-bottom / (R-top + R-bottom). Example fixture: 10 V, R-top 1000 ohms, target 5 V, solution R-bottom 1000 ohms. Treat it as an unloaded ideal divider.
2. RC filter: Inspector sees the target cutoff and formula; Analyst sees calculated cutoff; Operator selects R and C from small predefined sets. Use fc = 1 / (2*pi*R*C), with ohms and farads. Select a target reachable by the available settings and state the tolerance. A numeric cutoff display is enough.
3. Logic interlock: Inspector sees a Boolean expression and target, Analyst sees the output, Operator changes binary inputs. Use a small truth table and a clearly defined accepted condition. Do not assume only one solution unless the truth table proves it.

After every change the server evaluates progress. Rotate the three player roles between puzzles so each person operates once. Keep assignment ownership separate from game roles. Check that no view exposes both all hidden instructions and all controls. Randomize a small set of verified puzzle values between runs if time permits; otherwise acknowledge that a memorized solution can be replayed.

## 7. Checkpoints and dependencies

Planning assumption: each member can reserve about 5-7 focused hours per week, including integration and practice. This is a capacity assumption, not a proven estimate; revise after the first week. Use existing laptops, a local network and a common JDK. No hardware purchase is planned.

| Dates | Member 1 | Member 2 | Member 3 | Required evidence |
|---|---|---|---|---|
| 23-25 Sep | Concurrent three-client echo server | Client receiving without keyboard input | Divider logic plus two-thread state exercise | Each runs and explains their small program |
| 26 Sep-2 Oct | Join/roles and agreed protocol | Three role views and command encoding | First puzzle and role snapshots | Three clients finish puzzle 1 over localhost |
| 3-9 Oct | LAN configuration, error isolation | Host/port arguments and helpful errors | Filter/logic puzzles and transition tests | Complete three-puzzle game on separate laptops |
| 10-16 Oct | Rejoin sessions and heartbeat handling | Rejoin flow and receiver shutdown | Pause/resume, timer and concurrent-action tests | Disconnect one player and recover without resetting |
| 17-23 Oct | Server failure-case checks | Client invalid-input and clean-exit checks | Scripted network acceptance tests | All acceptance checks below pass; feature freeze |
| 24 Oct-6 Nov | Network explanation and setup review | User instructions and client explanation | Circuit calculations and test evidence | Clean-machine rehearsal and two mock vivas |
| 7-13 Nov | All: fixes, backups, rehearsal, final submission preparation | All | All | Reproducible final build; no new features |
| 14 Nov | Submit according to the lecturer's confirmed requirements | | | Submission completed |

Protocol agreement is the prerequisite for integrating client and server. Meanwhile, client development can use a small stub server, and the game engine can use offline commands. Working first-puzzle networking is the prerequisite for expanding scope. Member 1 coordinates the weekly integration; each author fixes their own failures. If the 2 October checkpoint slips, cut display polish and optional variation immediately.

## 8. What counts as done

Every task must have all four: code in the shared repository; exact run steps; observed test evidence; a two-minute explanation without reading generated prose. The other two members witness the weekly checkpoint. Screenshots alone do not prove independent execution or understanding.

Acceptance checks:

- Three distinct clients remain connected; leaving one idle does not block the others.
- Role assignment is unique; duplicate role/fourth-player joins fail clearly.
- One valid action produces consistent versioned role views; hidden data stays hidden.
- An Inspector attempting an Operator command is rejected by the server.
- Fragment a line over several writes and send multiple lines in one write: all commands are framed correctly.
- Malformed and oversized input is handled without taking down other sessions.
- Concurrent commands and timer ticks cannot skip a puzzle, double-complete it, or corrupt state.
- Close a client normally, kill its process, and separately interrupt its network: document detection behavior.
- Rejoin restores role and current puzzle; time stays paused until the resume condition is met.
- All three puzzles can be solved; the success state occurs once; expiry gives a clear failure state.
- Start from a clean checkout on another machine using the documented commands.

Keep a results table with date, test, expected result, actual result, and tester. Record real failures as well as fixes. No invented screenshots or results.

## 9. Managing progress without long PDFs

Send only the current task card. Assign one visible output per person for the next 48 hours. Ask for: a commit or source files, a 60-90 second recording, and three spoken answers. Limit work in progress to one main task each.

Twice weekly, hold a 15-minute call: each person runs their current feature, identifies a blocker, and accepts the next small task. If a task misses its checkpoint, pair for 20 minutes to locate the blocker and reduce the task. Repeated missed outputs trigger an explicit redistribution recorded by the team; do not conceal who did the work. A task is not done because someone says they read about it.

Each person keeps five bullet points: what I wrote, why it exists, what input it accepts, how it fails, and what test proves it. Each week another member explains that feature back to its author. AI help is acceptable only within course rules; the author must be able to trace and modify the resulting code.

## 10. Demonstration and viva

Suggested 8-minute demonstration, adjustable to the lecturer's allotted time:

1. Member 1 explains the architecture and starts the server (1 min).
2. Member 2 connects three clients on the LAN and identifies the roles (1 min).
3. All members solve a prepared short puzzle; Member 3 explains the equation and state change (2 min).
4. Show an unauthorized command being rejected and two clients remaining responsive while another is idle (1 min).
5. Disconnect and rejoin one client; show preserved state and timer behavior (1 min).
6. Show the remaining short puzzles and finish; point to test evidence and state known limits (2 min).

Use only a documented demonstration configuration if shortening the timer or puzzles; do not pretend a replay is live. Carry a backup recording and source archive, but treat these as backups. Verify LAN reachability, firewall rules and JDK versions beforehand; a localhost run alone does not prove a multi-computer demonstration.

Every member must answer: What is LO-5? What is LO-6? Where are sockets created? Why TCP? What does accept() do? What does readLine() wait for? Why is localhost wrong for a remote server? Why do we need threads? Which data is shared? What race does synchronization prevent? How does a SET become three role views? How does disconnect detection differ from reconnect recovery?

Member 1 extra: listening socket versus accepted socket; independent handlers; stream framing; role ownership; avoiding writes under the state lock.
Member 2 extra: keyboard blocking versus network blocking; receiver lifecycle; flushing; UTF-8; stale snapshots; what happens when the server closes.
Member 3 extra: divider/RC equations and units; allowed solutions; atomic check-and-update; timer races; why a thread-safe collection alone does not protect a multi-step game rule.

Mock-viva exercise: each person draws the system from memory, traces one command through actual code, explains one failure, then makes a small change and reruns a test. Rotate roles. This is more valuable than memorizing a report.

## 11. Risks and closure

Main risks: late team output, protocol mismatch, scope growth, concurrency bugs, LAN problems, and weak individual understanding. Small dated tasks address motivation; one protocol and early integration address mismatch; a feature freeze addresses scope; concurrent/failure tests address reliability; early LAN rehearsal addresses deployment; weekly teach-back addresses viva preparation.

Confirm the assessment format and lecturer-specific requirements; the supplied materials do not give a detailed viva rubric. Before submission, reconcile report claims with actual behavior, retain contribution history, package source/run instructions/test evidence, verify the final archive, and complete a full rehearsal. No plan guarantees marks or removes the need for every member to understand their work.
