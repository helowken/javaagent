package agent.server.command.executor;

import agent.base.utils.Logger;
import agent.base.utils.Pair;
import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand.TransformByFileCommand;
import agent.common.message.command.impl.ByRuleCommand.TransformByRuleCommand;
import agent.common.message.result.ExecResult;
import agent.hook.plugin.ClassFinder;
import agent.server.event.AgentEvent;
import agent.server.event.AgentEventListener;
import agent.server.event.EventListenerMgr;
import agent.server.event.impl.AdditionalTransformEvent;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;
import agent.server.transform.TransformResult;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.FileConfigParser;
import agent.server.transform.config.parser.RuleConfigParser;
import agent.server.transform.impl.dynamic.AdditionalClassTransformer;

import java.util.*;
import java.util.stream.Collectors;

import static agent.common.message.MessageType.CMD_TRANSFORM_BY_FILE;
import static agent.common.message.MessageType.CMD_TRANSFORM_BY_RULE;

public class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    private static final Logger logger = Logger.getLogger(TransformCmdExecutor.class);

    @Override
    ExecResult doExec(Command cmd) {
        int cmdType = cmd.getType();
        ConfigItem item;
        switch (cmdType) {
            case CMD_TRANSFORM_BY_FILE:
                item = new FileConfigParser.FileConfigItem(
                        ((TransformByFileCommand) cmd).getConfig()
                );
                break;
            case CMD_TRANSFORM_BY_RULE:
                TransformByRuleCommand ruleCmd = (TransformByRuleCommand) cmd;
                item = new RuleConfigParser.RuleConfigItem(
                        ruleCmd.getContext(),
                        ruleCmd.getClassName()
                );
                break;
            default:
                throw new RuntimeException("Invalid cmd type: " + cmdType);
        }

        AdditionalTransformListener listener = new AdditionalTransformListener();
        EventListenerMgr.reg(listener);
        try {
            List<TransformResult> resultList = new LinkedList<>(
                    TransformMgr.getInstance().transformByConfig(item)
            );
            resultList.addAll(
                    listener.getContextList()
                            .stream()
                            .map(TransformMgr.getInstance()::transform)
                            .collect(Collectors.toList())
            );
            return convert(resultList, cmdType, "Transform");
        } finally {
            EventListenerMgr.unreg(listener);
        }
    }

    private static class AdditionalTransformListener implements AgentEventListener {
        private final List<AdditionalTransformEvent> eventList = new LinkedList<>();

        @Override
        public void onNotify(AgentEvent event) {
            eventList.add((AdditionalTransformEvent) event);
        }

        private List<TransformContext> getContextList() {
            logger.debug("Event list: {}", eventList);
            ClassFinder classFinder = TransformMgr.getInstance().getClassFinder();
            Map<String, Pair<Set<Class<?>>, Map<String, Pair<ClassLoader, byte[]>>>> contextToPair = new HashMap<>();
            for (AdditionalTransformEvent event : eventList) {
                String context = event.getContext();
                Pair<Set<Class<?>>, Map<String, Pair<ClassLoader, byte[]>>> p = contextToPair.computeIfAbsent(
                        context,
                        key -> new Pair<>(new HashSet<>(), new HashMap<>())
                );
                event.getClassNameToBytes().forEach(
                        (className, bs) -> {
                            Class<?> clazz = classFinder.findClass(context, className);
                            p.left.add(clazz);
                            p.right.put(
                                    className,
                                    new Pair<>(clazz.getClassLoader(), bs)
                            );
                        }
                );
            }
            List<TransformContext> transformContextList = new ArrayList<>();
            contextToPair.forEach((context, pair) ->
                    transformContextList.add(
                            new TransformContext(
                                    context,
                                    pair.left,
                                    Collections.singletonList(
                                            new AdditionalClassTransformer(pair.right)
                                    ),
                                    false
                            )
                    )
            );
            logger.debug("Transform context list: {}", transformContextList);
            return transformContextList;
        }

        @Override
        public boolean accept(AgentEvent event) {
            return AdditionalTransformEvent.EVENT_TYPE.equals(event.getType());
        }
    }

}
