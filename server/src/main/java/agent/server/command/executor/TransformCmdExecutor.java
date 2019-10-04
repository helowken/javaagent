package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand.TransformByFileCommand;
import agent.common.message.command.impl.ByRuleCommand.TransformByRuleCommand;
import agent.common.message.result.ExecResult;
import agent.server.event.EventListenerMgr;
import agent.server.transform.TransformMgr;
import agent.server.transform.TransformResult;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.FileConfigParser;
import agent.server.transform.config.parser.RuleConfigParser;
import agent.server.transform.impl.dynamic.AdditionalTransformListener;

import java.util.LinkedList;
import java.util.List;

import static agent.common.message.MessageType.CMD_TRANSFORM_BY_FILE;
import static agent.common.message.MessageType.CMD_TRANSFORM_BY_RULE;

public class TransformCmdExecutor extends AbstractTransformCmdExecutor {
    private static final String PREFIX = "Transform";

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
        return doTransform(item, cmdType);

//        Logger logger = Logger.getLogger(TransformCmdExecutor.class);
//        if (context != null) {
//            Collection<String> invalidClassNames = InvalidClassNameCache.getInstance().getInvalidClassNames(context);
//            logger.debug("Invalid class names: {}", invalidClassNames);
//            final String contextPath = context;
//            Set<Class<?>> classSet = invalidClassNames.stream()
//                    .map(
//                            className -> {
//                                try {
//                                    return TransformMgr.getInstance().getClassFinder().findClass(contextPath, className);
//                                } catch (Exception e) {
//                                    logger.error("Get class failed by name: {}", e, className);
//                                    return null;
//                                }
//                            }
//                    )
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toSet());
//            if (!classSet.isEmpty()) {
//                logger.debug("Class set: {}", classSet);
//                TransformContext transformContext = new TransformContext(
//                        context,
//                        classSet,
//                        Collections.singletonList(
//                                new SaveClassByteCodeTransformer()
//                        ),
//                        false
//                );
//                transformContext.setRefClassSet(Collections.emptySet());
//                TransformMgr.getInstance().transform(transformContext);
//            }
//        }
    }

    private ExecResult doTransform(ConfigItem item, int cmdType) {
        AdditionalTransformListener listener = new AdditionalTransformListener();
        EventListenerMgr.reg(listener);
        try {
            List<TransformResult> resultList = new LinkedList<>(
                    TransformMgr.getInstance().transformByConfig(item)
            );
            resultList.addAll(
                    TransformMgr.getInstance().transform(
                            listener.getContextList()
                    )
            );
            return convert(
                    resultList,
                    cmdType,
                    PREFIX);
        } finally {
            EventListenerMgr.unreg(listener);
        }
    }

}
