package agent.base.utils;

public class ShutdownUtils {
    public static void addHook(Runnable runnable, String threadName) {
        Runtime.getRuntime().addShutdownHook(
                new Thread(
                        runnable,
                        Constants.AGENT_THREAD_PREFIX + threadName
                )
        );
    }
}
