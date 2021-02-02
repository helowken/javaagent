package agent.server.utils;

import agent.base.utils.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRunner {
    private static final Logger logger = Logger.getLogger(TaskRunner.class);
    private final ExecutorService executor;
    private final AtomicInteger jobsCount = new AtomicInteger(0);
    private final Object lock = new Object();
    private boolean end = true;

    public TaskRunner(ExecutorService executor) {
        this.executor = executor;
    }

    public void run(AgentTask task) {
        jobsCount.incrementAndGet();
        synchronized (lock) {
            end = false;
        }
        executor.submit(
                () -> {
                    try {
                        task.run();
                    } catch (Throwable e) {
                        logger.error("Run task failed.", e);
                    } finally {
                        int rest = jobsCount.decrementAndGet();
                        if (rest == 0) {
                            synchronized (lock) {
                                end = true;
                                lock.notifyAll();
                            }
                        } else
                            logger.debug("Jobs count: {}", rest);
                    }
                }
        );
    }

    public void await() {
        try {
            synchronized (lock) {
                while (!end) {
                    lock.wait();
                }
            }
        } catch (InterruptedException e) {
            logger.error("Interrupted.", e);
        }
    }

    public interface AgentTask {
        void run() throws Exception;
    }
}
