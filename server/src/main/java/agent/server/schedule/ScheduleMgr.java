package agent.server.schedule;

import agent.base.utils.Constants;
import agent.base.utils.LockObject;
import agent.base.utils.Logger;
import agent.common.config.ScheduleConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ScheduleMgr {
    private static final Logger logger = Logger.getLogger(ScheduleMgr.class);
    private static final ScheduleMgr instance = new ScheduleMgr();
    private final Map<String, ScheduleHandle> keyToTask = new HashMap<>();
    private final LockObject lo = new LockObject();

    public static ScheduleMgr getInstance() {
        return instance;
    }

    private ScheduleMgr() {
    }

    public void exec(ScheduleConfig config, ScheduleTask task) {
        lo.syncValue(
                lock -> {
                    String key = config.getKey();
                    if (keyToTask.containsKey(key))
                        throw new RuntimeException("Schedule task key existed: " + key);
                    ScheduleHandle st = createHandle(config, task);
                    keyToTask.put(key, st);
                    return st;
                }
        ).run();
    }

    private ScheduleHandle createHandle(ScheduleConfig config, ScheduleTask task) {
        return config.getRepeatCount() > 0 ?
                new CountBasedHandle(config, task) :
                new TimeBasedHandle(config, task);
    }

    public void stop(String key) {
        ScheduleHandle task = lo.syncValue(
                lock -> keyToTask.remove(key)
        );
        if (task != null)
            task.stop();
        else
            throw new RuntimeException("No schedule task found by key: " + key);
    }

    private static abstract class ScheduleHandle {
        private final ScheduleConfig config;
        private final ScheduleTask task;
        private final Timer timer = new Timer(
                Constants.AGENT_THREAD_PREFIX + "Timer",
                true
        );
        private int count = 0;

        ScheduleHandle(ScheduleConfig config, ScheduleTask task) {
            this.config = config;
            this.task = task;
        }

        void run() {
            try {
                task.preRun();
            } catch (Throwable e) {
                logger.error("Schedule task preRun failed: {}", e, config.getKey());
            }
            timer.schedule(
                    new DefaultTimerTask(this),
                    config.getDelayMs(),
                    config.getIntervalMs()
            );
        }

        void doRun() {
            try {
                ++count;
                logger.debug("Schedule task run count: {}", count);
                task.run();
            } catch (Exception e) {
                logger.error("Schedule task run failed: {}", e, config.getKey());
            }
        }

        void end() {
            try {
                task.finish();
            } catch (Throwable e) {
                logger.error("Schedule task finish failed: {}", e, config.getKey());
            }
        }

        void stop() {
            timer.cancel();
            try {
                task.postRun();
            } catch (Throwable e) {
                logger.error("Task post run failed: {}", e, config.getKey());
            }
        }

        abstract boolean isFinished();
    }

    private static class DefaultTimerTask extends TimerTask {
        private final ScheduleHandle handle;

        private DefaultTimerTask(ScheduleHandle handle) {
            this.handle = handle;
        }

        @Override
        public void run() {
            if (handle.isFinished()) {
                handle.end();
                ScheduleMgr.getInstance().stop(
                        handle.config.getKey()
                );
                return;
            }
            handle.doRun();
        }
    }

    private static class TimeBasedHandle extends ScheduleHandle {
        private final long endTime;

        private TimeBasedHandle(ScheduleConfig config, ScheduleTask task) {
            super(config, task);
            this.endTime = System.currentTimeMillis() + config.getTotalTimeMs();
        }

        @Override
        boolean isFinished() {
            return System.currentTimeMillis() >= endTime;
        }
    }

    private static class CountBasedHandle extends ScheduleHandle {
        private final int repeatCount;
        private int count = 0;

        CountBasedHandle(ScheduleConfig config, ScheduleTask task) {
            super(config, task);
            this.repeatCount = config.getRepeatCount();
        }

        @Override
        boolean isFinished() {
            return count >= repeatCount;
        }

        @Override
        void doRun() {
            ++count;
            super.doRun();
        }
    }
}
