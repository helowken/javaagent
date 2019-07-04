package agent.server.command.executor;

import agent.common.message.command.Command;
import agent.common.message.command.impl.TestConfigCommand;
import agent.common.message.result.DefaultExecResult;
import agent.common.message.result.ExecResult;
import agent.server.transform.impl.MethodFinder;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.ModuleConfig;
import agent.server.utils.JSONUtils;
import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TestConfigCmdExecutor extends AbstractCmdExecutor {
    private static final Logger logger = Logger.getLogger(TestConfigCmdExecutor.class);
    private static final String SELF = "self";

    @Override
    ExecResult doExec(Command cmd) throws Exception {
        Map<ModuleConfig, List<MethodFinder.MethodSearchResult>> moduleToSearchResult =
                TransformMgr.getInstance().searchMethods(((TestConfigCommand) cmd).getConfig());
        Map<String, Map<String, Map<String, List<String>>>> rsMap = new HashMap<>();
        moduleToSearchResult.forEach((moduleConfig, searchResultList) -> {
            rsMap.computeIfAbsent(moduleConfig.getContextPath(), contextPath -> {
                Map<String, Map<String, List<String>>> classToMethods = new HashMap<>();
                searchResultList.forEach(searchResult -> {
                    classToMethods.computeIfAbsent(searchResult.ctClass.getName(), className -> {
                        Map<String, List<String>> declaringClassToMethods = new HashMap<>();
                        searchResult.methodList.forEach(method -> {
                            String declareClass = method.getDeclaringClass().getName();
                            if (className.equals(declareClass))
                                declareClass = SELF;
                            declaringClassToMethods.computeIfAbsent(declareClass, key -> new ArrayList<>())
                                    .add(method.getName() + method.getSignature());
                        });
                        return declaringClassToMethods;
                    });
                });
                return classToMethods;
            });
        });
        logger.debug("Content: \n{}", JSONUtils.writeAsString(rsMap));
        return DefaultExecResult.toSuccess(cmd.getType(), null, rsMap);
    }
}
