package agent.server.command.executor;

import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.TypeObject;
import agent.common.config.ModuleConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.common.utils.JsonUtils;
import agent.invoke.DestInvoke;
import agent.server.transform.TransformMgr;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

class SearchCmdExecutor extends AbstractCmdExecutor {

    @Override
    ExecResult doExec(Command cmd) {
        ModuleConfig moduleConfig = JsonUtils.convert(
                ((MapCommand) cmd).getConfig(),
                new TypeObject<ModuleConfig>() {
                }
        );
        return DefaultExecResult.toSuccess(
                cmd.getType(),
                null,
                search(moduleConfig)
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
