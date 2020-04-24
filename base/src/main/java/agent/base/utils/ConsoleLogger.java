package agent.base.utils;

public class ConsoleLogger extends AbstractLoggerImpl {
    private static final ConsoleLogger instance = new ConsoleLogger();
    private static final String PREFIX = "[SYS]: ";

    public static ConsoleLogger getInstance() {
        return instance;
    }

    private ConsoleLogger() {
    }

    @Override
    protected void println(String prefix, String pattern, Throwable t, Object... pvs) {
        String s = PREFIX + formatMsg(pattern, pvs);
        if (t != null)
            s += "\nError: " + t.getMessage();
        System.err.println(s);
    }

    @Override
    protected LoggerLevel getLevel() {
        return LoggerLevel.INFO;
    }
}
