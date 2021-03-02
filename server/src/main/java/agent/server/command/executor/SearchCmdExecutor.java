package agent.server.command.executor;

import agent.base.utils.InvokeDescriptorUtils;
import agent.cmdline.command.Command;
import agent.cmdline.command.execute.AbstractCmdExecutor;
import agent.cmdline.command.result.DefaultExecResult;
import agent.cmdline.command.result.ExecResult;
import agent.common.config.ModuleConfig;
import agent.invoke.DestInvoke;
import agent.server.transform.TransformMgr;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

class SearchCmdExecutor extends AbstractCmdExecutor {

    @Override
    protected ExecResult doExec(Command cmd) {
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                null,
                search(
                        cmd.getContent()
                )
        );
    }

    private Object search(ModuleConfig moduleConfig) {
        Collection<DestInvoke> invokes = TransformMgr.getInstance().searchInvokes(moduleConfig);
        if (!invokes.isEmpty()) {
            Map<String, Collection<String>> classToInvokes = new TreeMap<>();
            invokes.forEach(
                    invoke -> classToInvokes.computeIfAbsent(
                            invoke.getDeclaringClass().getName(),
                            className -> new TreeSet<>()
                    ).add(
                            InvokeDescriptorUtils.descToText(
                                    invoke.getFullName()
                            )
                    )
            );
            return classToInvokes;
        }
        return null;
    }
}
