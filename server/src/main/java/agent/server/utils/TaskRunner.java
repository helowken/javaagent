package agent.server.utils;

import agent.base.utils.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskRunner {
    private static final Logger logger = Logger.getLogger(TaskRunner.class);
    private final ExecutorService executor;
    private final AtomicInteger jobsCount = new AtomicInteger(0);
    private final CountDownLatch latch = new CountDownLatch(1);

    public TaskRunner(ExecutorService executor) {
        this.executor = executor;
    }

    public void run(AgentTask task) {
        jobsCount.incrementAndGet();
        executor.submit(
                () -> {
                    try {
                        task.run();
                    } catch (Throwable e) {
                        logger.error("Run task failed.", e);
                    } finally {
                        if (jobsCount.decrementAndGet() == 0)
                            latch.countDown();
                        else
                            logger.debug("Jobs count: {}", jobsCount);
                    }
                }
        );
    }

    public void await() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Interrupted.", e);
        }
    }

    public interface AgentTask {
        void run() throws Exception;
    }
}
