package agent.base.utils;

public class ConsoleLogger extends AbstractLoggerImpl {
    private static final ConsoleLogger instance = new ConsoleLogger();

    public static ConsoleLogger getInstance() {
        return instance;
    }

    private ConsoleLogger() {
    }

    @Override
    protected void println(String prefix, String pattern, Throwable t, Object... pvs) {
        String s = formatMsg(pattern, pvs);
        if (t != null)
            s += "\nError: " + t.getMessage();
        System.out.println(s);
        System.out.flush();
    }

    @Override
    protected LoggerLevel getLevel() {
        return LoggerLevel.INFO;
    }
}
