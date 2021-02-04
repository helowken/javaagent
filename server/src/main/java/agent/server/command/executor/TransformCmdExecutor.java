package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformLock;
import agent.server.transform.TransformMgr;
import agent.server.transform.TransformResult;
import agent.server.transform.TransformerRegistry;
import agent.server.transform.tools.asm.ProxyTransformMgr;

class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Transform";

    @Override
    ExecResult doExec(Command cmd) {
        return convert(
                TransformLock.useLock(
                        () -> {
                            TransformResult result = TransformMgr.getInstance().transformByConfig(
                                    cmd.getContent()
                            );
                            TransformerRegistry.removeTransformers(
                                    ProxyTransformMgr.getInstance().getNotUsedCalls(
                                            TransformerRegistry.getTids()
                                    )
                            );
                            return result;
                        }
                ),
                cmd.getType(),
                PREFIX
        );
    }

}
