package agent.launcher.assist;

import java.lang.instrument.Instrumentation;

public class AssistLauncher {
    private static Instrumentation assistInstrumentation;
    private static final Object lock = new Object();

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        init(instrumentation);
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        init(instrumentation);
    }

    private static void init(Instrumentation instrumentation) {
        assistInstrumentation = instrumentation;
        Thread thread = new Thread(() -> {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static Instrumentation getInstrumentation() {
        synchronized (lock) {
            try {
                return assistInstrumentation;
            } finally {
                lock.notifyAll();
            }
        }
    }
}
