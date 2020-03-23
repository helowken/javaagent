package agent.client.utils;

public class ClientLogger {
    private static final String PREFIX = "[SYS]: ";

    public static void info(String msg) {
        System.out.println(PREFIX + msg);
    }

    public static void error(String msg) {
        System.err.println(PREFIX + msg);
    }

}
