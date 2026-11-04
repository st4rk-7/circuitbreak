public interface Stage {
    String name();
    String handle(Role who, String cmd);
    boolean isSolved();
}
