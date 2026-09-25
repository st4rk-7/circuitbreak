# Member 3: puzzle rules and shared state

First checkpoint: 25 September 2026. Timebox: two focused sessions; report blockers early.

Your job: make an offline Java voltage-divider puzzle. Use Vin = 10 V and Rtop = 1000 ohms. Choose Rbottom from 500, 1000 or 2000 ohms. Calculate Vout = Vin * Rbottom / (Rtop + Rbottom); target 5 V. Validate the choices and generate separate Inspector, Analyst and Operator views. Keep the calculation independent of sockets and printing.

Add a small two-thread exercise: have two threads request completion of the same puzzle at the same time. Protect the check-and-update so completion is recorded exactly once. Explain which state is shared and what could go wrong without that protection.

Deliver: source, run instructions, checks for all three resistor choices, one invalid choice and concurrent completion, plus a 60-90 second recording. Explain floating-point division, the formula, and the synchronization boundary.

LO-6 proof: safe shared state. LO-5 contribution comes at integration: validate network actions and produce network role views; then write a small socket test driver. The offline calculation alone does not demonstrate LO-5.

Next assignment after this passes: connect your GameRoom to Member 1's handler through the agreed interface. Own puzzle tests, timer rules and the later filter/logic puzzles.
