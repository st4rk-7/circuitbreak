# Member 2: player client

First checkpoint: 25 September 2026. Timebox: two focused sessions; report blockers early.

Your job: starting from clientchat.java, create a client that keeps receiving server messages while the user has not typed anything. Use one receiver thread and keep keyboard input on the main thread. Accept server host and port as arguments. Show a useful error if connection fails. Keep the original exercise unchanged.

Deliver: your client code, compile/run instructions, and a 60-90 second recording showing incoming messages arriving without pressing Enter. A tiny stub server that sends periodic lines is enough before Member 1's server is ready. Demonstrate closing the connection; explain how the receiver terminates and how console input affects process shutdown.

Explain: why the old client only reads after sending; what readLine() waits for; why localhost works only when the server is on the same machine.

LO-5 proof: connect, send and receive. LO-6 proof: keyboard waiting does not block network reception.

Next assignment after this passes: three role views, message encoding, and server-error display. Own all client defects and user run instructions.
