/**
 * The contract between the server (Member 1) and the door logic (Member 3).
 * The server only ever calls these three methods. Do not change them without telling the team.
 */
public interface Stage {
    String name();                       // "POWER" or "LOCK"
    String handle(Role who, String cmd); // returns the reply text for that player
    boolean isSolved();
}
