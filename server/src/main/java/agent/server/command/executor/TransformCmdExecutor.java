package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.ByFileCommand.TransformByFileCommand;
import agent.common.message.command.impl.ByRuleCommand.TransformByRuleCommand;
import agent.common.message.result.ExecResult;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;
import agent.server.transform.TransformResult;
import agent.server.transform.config.parser.ConfigItem;
import agent.server.transform.config.parser.FileConfigParser;
import agent.server.transform.config.parser.RuleConfigParser;
import agent.server.transform.impl.TransformSession;
import agent.server.transform.impl.UpdateClassDataTransformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
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
        TransformSession.clear();
        try {
            List<TransformResult> resultList = TransformMgr.getInstance().transformByConfig(item);
            updateClassData();
            return convert(resultList, cmdType, PREFIX);
        } finally {
            TransformSession.clear();
        }
    }

    private void updateClassData() {
        List<TransformContext> transformContextList = new ArrayList<>();
        TransformSession.get().getContextToClassSet().forEach(
                (context, classToData) -> transformContextList.add(
                        new TransformContext(
                                context,
                                new HashSet<>(classToData.keySet()),
                                Collections.singletonList(
                                        new UpdateClassDataTransformer(classToData)
                                ),
                                false
                        )
                )
        );
        TransformMgr.getInstance().transform(transformContextList);
    }

}
