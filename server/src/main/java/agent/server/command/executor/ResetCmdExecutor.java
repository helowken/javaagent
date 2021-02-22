package agent.server.command.executor;

import agent.common.config.ResetConfig;
import agent.cmdline.command.Command;
import agent.common.message.result.ExecResult;
import agent.invoke.DestInvoke;
import agent.server.transform.TransformLock;
import agent.server.transform.TransformMgr;
import agent.server.transform.TransformResult;
import agent.server.transform.TransformerRegistry;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.search.filter.ClassFilter;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.InvokeFilter;
import agent.server.transform.tools.asm.ProxyTransformMgr;

import java.util.*;
import java.util.stream.Collectors;

class ResetCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Reset";

    @Override
    ExecResult doExec(Command cmd) {
        ResetConfig config = cmd.getContent();
        config.validate();

        TransformResult transformResult = new TransformResult();
        TransformLock.useLock(
                () -> {
                    Collection<String> tids;
                    if (config.isPrune()) {
                        tids = TransformerRegistry.getTids();
                        ProxyTransformMgr.getInstance().prune(
                                reset(
                                        transformResult,
                                        findClassList(config)
                                )
                        );
                    } else {
                        tids = config.getTids();
                        if (tids == null || tids.isEmpty())
                            tids = TransformerRegistry.getTids();
                        ProxyTransformMgr.getInstance().removeCallSites(
                                findInvokeIds(config),
                                tids
                        );
                    }

                    TransformerRegistry.removeTransformers(
                            ProxyTransformMgr.getInstance().getNotUsedCalls(tids)
                    );
                    return null;
                }
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
                classList,
                clazz -> clazz
        );
    }

    private List<Class<?>> findClassList(ResetConfig config) {
        return DestInvokeIdRegistry.getInstance().run(
                classToInvokeToId -> doFindClassList(config, classToInvokeToId)
        );
    }

    private List<Class<?>> doFindClassList(ResetConfig config, Map<Class<?>, Map<DestInvoke, Integer>> classToInvokeToId) {
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
        return classToInvokeToId.keySet()
                .stream()
                .filter(filter::accept)
                .collect(Collectors.toList());
    }

    private Collection<Integer> findInvokeIds(ResetConfig config) {
        InvokeFilter methodFilter = FilterUtils.newInvokeFilter(
                config.getTargetConfig().getMethodFilter()
        );
        InvokeFilter constructorFilter = FilterUtils.newInvokeFilter(
                config.getTargetConfig().getConstructorFilter()
        );
        return DestInvokeIdRegistry.getInstance().run(
                classToInvokeToId -> {
                    Set<Integer> invokeIds = new HashSet<>();
                    doFindClassList(config, classToInvokeToId).forEach(
                            clazz -> {
                                Map<DestInvoke, Integer> invokeToId = classToInvokeToId.get(clazz);
                                if (invokeToId != null) {
                                    invokeToId.forEach(
                                            (invoke, id) -> {
                                                InvokeFilter filter = null;
                                                switch (invoke.getType()) {
                                                    case CONSTRUCTOR:
                                                        filter = constructorFilter;
                                                        break;
                                                    case METHOD:
                                                        filter = methodFilter;
                                                        break;
                                                }
                                                if (filter == null || FilterUtils.isAccept(filter, invoke))
                                                    invokeIds.add(id);
                                            }
                                    );
                                }
                            }
                    );
                    return invokeIds;
                }
        );
    }
}
