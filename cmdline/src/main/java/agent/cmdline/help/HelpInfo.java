package agent.cmdline.help;

public interface HelpInfo {
    void print(StringBuilder sb);

    default void testPrint() {
        StringBuilder sb = new StringBuilder();
        print(sb);
        System.out.println(sb);
    }
}
