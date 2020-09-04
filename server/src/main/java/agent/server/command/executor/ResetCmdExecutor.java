package agent.server.command.executor;

import agent.base.utils.TypeObject;
import agent.common.config.ResetConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;
import agent.common.message.result.ExecResult;
import agent.common.utils.JsonUtils;
import agent.server.transform.TransformMgr;
import agent.server.transform.TransformResult;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.UpdateClassDataTransformer;
import agent.server.transform.revision.ClassDataRepository;
import agent.server.transform.search.filter.ClassFilter;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.tools.asm.ProxyTransformMgr;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

class ResetCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Reset";

    @Override
    ExecResult doExec(Command cmd) {
        ResetConfig config = JsonUtils.convert(
                ((MapCommand) cmd).getConfig(),
                new TypeObject<ResetConfig>() {
                }
        );

        TransformResult transformResult = new TransformResult();
        ProxyTransformMgr.getInstance().reset(
                reset(
                        transformResult,
                        findClassList(config)
                )
        );

        return convert(
                transformResult,
                cmd.getType(),
                PREFIX
        );
    }

    private List<Class<?>> reset(TransformResult transformResult, List<Class<?>> classList) {
        return TransformMgr.getInstance().doReTransform(
                transformResult,
                new UpdateClassDataTransformer(
                        ClassDataRepository.getInstance()::getOriginalClassData
                ),
                classList,
                clazz -> clazz
        );
    }

    private List<Class<?>> findClassList(ResetConfig config) {
        ClassFilter filter = Optional.ofNullable(
                FilterUtils.newClassFilter(
                        config.getTargetConfig().getClassFilter(),
                        true
                )
        ).orElseGet(
                () -> FilterUtils.newClassFilter(
                        Collections.singleton("*"),
                        Collections.emptySet(),
                        true
                )
        );
        if (filter == null)
            throw new RuntimeException("No Class filter!");
        return DestInvokeIdRegistry.getInstance().run(
                classToInvokeToId -> classToInvokeToId.keySet()
                        .stream()
                        .filter(filter::accept)
                        .collect(Collectors.toList())
        );
    }

}
