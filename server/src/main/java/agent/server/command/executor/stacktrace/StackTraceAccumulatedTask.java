package agent.server.command.executor.stacktrace;

import agent.common.config.StackTraceConfig;
import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructContext;
import agent.common.tree.Tree;
import agent.server.utils.log.LogMgr;

import static agent.server.command.executor.stacktrace.StackTraceUtils.convertStackTraceToTree;

public class StackTraceAccumulatedTask extends AbstractStackTraceTask {
    private static final StructContext context = new StructContext();
    private final Tree<StackTraceCountItem> tree = new Tree<>();

    public StackTraceAccumulatedTask(StackTraceConfig config) {
        super(config);
    }

    @Override
    public void run() {
        convertStackTraceToTree(
                tree,
                getStackTraces(),
                true,
                thread -> getNameId(
                        thread.getName()
                ),
                el -> getNameId(
                        el.getClassName()
                ),
                el -> getNameId(
                        el.getMethodName()
                )
        );
    }

    @Override
    void onFinish() {
        LogMgr.logBinary(
                logKey,
                buf -> Struct.serialize(buf, tree, context)
        );
    }
}
