# Member 1: server and coordination

First checkpoint: 25 September 2026. Timebox: two focused sessions; report blockers early.

Your job: starting from serverchat.java, make a TCP server keep three clients connected at once. Remove keyboard replies from the server. Give each connection a handler thread and an ID; echo messages with that ID. If one client exits, the others must keep working. Keep the original exercise unchanged.

Deliver: your server code, compile/run instructions, and a 60-90 second recording showing three clients, one idle client, and one disconnect. Explain accept(), why the old server handles conversations sequentially, and why start() differs from calling run().

LO-5 proof: receive and reply through sockets. LO-6 proof: three client handlers progress independently.

Next assignment after this passes: agree the shared protocol with the other members; add unique role assignment and routing to GameRoom. You own networking defects and coordination, not everyone's unfinished tasks.
