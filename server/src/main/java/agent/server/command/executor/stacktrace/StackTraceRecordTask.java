package agent.server.command.executor.stacktrace;

import agent.common.config.StackTraceConfig;
import agent.common.struct.impl.*;
import agent.server.utils.log.LogMgr;

public class StackTraceRecordTask extends AbstractStackTraceTask {
    private static final StructContext context = new StructContext();
    private static final ThreadLocal<StackTraceRecordTask> taskLocal = new ThreadLocal<>();

    static {
        context.addPojoInfo(
                Thread.class::isAssignableFrom,
                new PojoInfo<>(
                        StackTraceEntity.POJO_TYPE,
                        null,
                        new PojoFieldPropertyList<>(
                                new PojoFieldProperty<>(Long.class, 0, null, Thread::getId),
                                new PojoFieldProperty<>(
                                        Integer.class,
                                        1,
                                        null,
                                        thread -> taskLocal.get().getNameId(
                                                thread.getName()
                                        )
                                )
                        )
                )
        ).addPojoInfo(
                StackTraceElement.class,
                new PojoInfo<>(
                        StackTraceCountItem.POJO_TYPE,
                        null,
                        new PojoFieldPropertyList<>(
                                new PojoFieldProperty<>(
                                        Integer.class,
                                        0,
                                        null,
                                        el -> taskLocal.get().getNameId(
                                                el.getClassName()
                                        )
                                ),
                                new PojoFieldProperty<>(
                                        Integer.class,
                                        1,
                                        null,
                                        el -> taskLocal.get().getNameId(
                                                el.getMethodName()
                                        )
                                )
                        )
                )
        );
    }

    public StackTraceRecordTask(StackTraceConfig config) {
        super(config);
    }

    @Override
    void onFinish() {
    }

    @Override
    public void run() {
        taskLocal.set(this);
        try {
            Object o = getStackTraces();
            LogMgr.logBinary(
                    logKey,
                    buf -> Struct.serialize(buf, o, context)
            );
        } finally {
            taskLocal.remove();
        }
    }
}
