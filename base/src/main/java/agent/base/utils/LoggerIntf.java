package agent.base.utils;

interface LoggerIntf {
    void info(String pattern, Object... pvs);

    void debug(String pattern, Object... pvs);

    void warn(String pattern, Object... pvs);

    void error(String pattern, Object... pvs);

    void error(String pattern, Throwable t, Object... pvs);
}
