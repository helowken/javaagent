package agent.server.transform;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.launcher.instrument.InstrumentSupplyLauncher;
import agent.server.ServerListener;
import agent.server.utils.TaskRunner;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

public class InstrumentationMgr implements ServerListener {
    private static final Logger logger = Logger.getLogger(InstrumentationMgr.class);
    private static final InstrumentationMgr instance = new InstrumentationMgr();
    private static final int QUEUE_SIZE = 20;
    private BlockingQueue<Instrumentation> instrumentationQueue;
    private ThreadPoolExecutor executor;

    public static InstrumentationMgr getInstance() {
        return instance;
    }

    private InstrumentationMgr() {
    }

    @Override
    public void onStartup(Object[] args) {
        List<Instrumentation> instrumentationList = new ArrayList<>(
                InstrumentSupplyLauncher.getInstrumentationList()
        );
        instrumentationList.add(
                Utils.getArgValue(args, 0)
        );
        int size = instrumentationList.size();
        instrumentationQueue = new ArrayBlockingQueue<>(size);
        instrumentationQueue.addAll(instrumentationList);
        executor = new ThreadPoolExecutor(
                size,
                size,
                0,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>()
        );
    }

    @Override
    public void onShutdown() {
    }

    public Class<?>[] getInitiatedClasses(ClassLoader classLoader) {
        return runAndGet(
                instrumentation -> instrumentation.getInitiatedClasses(classLoader)
        );
    }

    public Class<?>[] getAllLoadedClasses() {
        return runAndGet(Instrumentation::getAllLoadedClasses);
    }

    public <T> T runAndGet(InstrumentationTask<T> task) {
        try {
            return submit(task).get();
        } catch (Throwable t) {
            logger.error("get result failed.", t);
        }
        return null;
    }

    public void retransform(RetransformItem item) {
        retransform(
                Collections.singletonList(item)
        );
    }

    public void retransform(List<RetransformItem> items) {
        if (items.size() == 1) {
            runAndGet(
                    convert(
                            items.get(0)
                    )
            );
        } else if (!items.isEmpty()) {
            TaskRunner taskRunner = new TaskRunner(executor);
            items.stream()
                    .map(
                            item -> convert(
                                    convert(item)
                            )
                    )
                    .forEach(
                            callable -> taskRunner.run(callable::call)
                    );
            taskRunner.await();
        }
    }

    private <T> InstrumentationTask<T> convert(RetransformItem item) {
        return instrumentation -> {
            long st = System.currentTimeMillis();
            try {
                instrumentation.addTransformer(item.transformer, item.canRetransform);
                instrumentation.retransformClasses(item.clazz);
            } catch (Throwable t) {
                if (item.errorHandler != null)
                    item.errorHandler.handle(item.clazz, t);
                else
                    logger.error("retransform failed.", t);
            } finally {
                instrumentation.removeTransformer(item.transformer);
                long et = System.currentTimeMillis();
                logger.debug(item.msg + ": {}", (et - st));
            }
            return null;
        };
    }

    private <T> Callable<T> convert(InstrumentationTask<T> task) {
        return () -> {
            Instrumentation instrumentation = null;
            try {
                instrumentation = instrumentationQueue.take();
                return task.run(instrumentation);
            } finally {
                if (instrumentation != null)
                    instrumentationQueue.add(instrumentation);
            }
        };
    }

    private <T> Future<T> submit(InstrumentationTask<T> task) {
        return executor.submit(
                convert(task)
        );
    }

    public interface InstrumentationTask<T> {
        T run(Instrumentation instrumentation);
    }

    public static class RetransformItem {
        final Class<?> clazz;
        final ClassFileTransformer transformer;
        final boolean canRetransform;
        final RetransformClassErrorHandler errorHandler;
        final String msg;

        public RetransformItem(Class<?> clazz, ClassFileTransformer transformer, boolean canRetransform, RetransformClassErrorHandler errorHandler, String msg) {
            this.clazz = clazz;
            this.transformer = transformer;
            this.canRetransform = canRetransform;
            this.errorHandler = errorHandler;
            this.msg = msg;
        }

        @Override
        public String toString() {
            return "RetransformItem{" +
                    "clazz=" + clazz +
                    ", transformer=" + transformer +
                    ", msg='" + msg + '\'' +
                    '}';
        }
    }

    public interface RetransformClassErrorHandler {
        void handle(Class<?> clazz, Throwable e);
    }
}
