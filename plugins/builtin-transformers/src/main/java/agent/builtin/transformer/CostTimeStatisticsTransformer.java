package agent.builtin.transformer;

import agent.base.utils.MethodDescriptorUtils;
import agent.base.utils.TypeObject;
import agent.builtin.transformer.utils.CostTimeLogger;
import agent.builtin.transformer.utils.LogUtils;
import agent.common.utils.JSONUtils;
import agent.server.transform.MethodFinder;
import agent.server.transform.MethodFinder.MethodSearchResult;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.cp.AgentClassPool;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.TargetClassConfig;
import agent.server.transform.impl.TransformerInfo;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogConfig;
import agent.server.utils.log.LogMgr;
import javassist.CtMethod;

import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unchecked")
public class CostTimeStatisticsTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "sys_costTimeStatistics";
    private static final String KEY_ENTRY_POINT = "entryPoint";

    private String logKey;
    private Set<String> entryPoints = new HashSet<>();

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        logKey = LogMgr.regBinary(config, Collections.EMPTY_MAP);
        configEntryPoint(config);
    }

    private void configEntryPoint(Map<String, Object> config) {
        List<Map<String, Object>> entryPointConfig = (List) Optional.ofNullable(
                config.get(KEY_ENTRY_POINT)
        ).orElseThrow(
                () -> new RuntimeException("No entry point found in config.")
        );
        List<ClassConfig> classConfigList = JSONUtils.convert(
                entryPointConfig,
                new TypeObject<List<ClassConfig>>() {
                }
        );
        TransformerInfo transformerInfo = getTransformerInfo();
        List<TargetClassConfig> targetClassConfigList = TransformMgr.getInstance().convert(
                transformerInfo.getContext(),
                classConfigList
        );
        targetClassConfigList.forEach(
                targetClassConfig -> {
                    MethodSearchResult result = MethodFinder.getInstance().find(targetClassConfig);
                    result.methods.forEach(method ->
                            entryPoints.add(
                                    getEntryPoint(
                                            transformerInfo.getContext(),
                                            method
                                    )
                            )
                    );
                }
        );
        LogConfig logConfig = LogMgr.getBinaryLogConfig(logKey);
        CostTimeLogger.getInstance().regOutputPath(
                transformerInfo.getContext(),
                logConfig.getOutputPath()
        );
    }

    private String getEntryPoint(String context, Method method) {
        return context + ":" + MethodDescriptorUtils.getLongName(method);
    }

    private boolean isEntryPoint(String context, Method method) {
        return entryPoints.contains(
                getEntryPoint(context, method)
        );
    }

    protected AgentClassPool getClassPool() {
        return null;
    }
    @Override
    protected void transformMethod(Method method) throws Exception {
        String context = getTransformerInfo().getContext();
        String loggerExpr = CostTimeLogger.class.getName() + ".getInstance()";
        int type = CostTimeLogger.getInstance().reg(
                context,
                method.getDeclaringClass().getName(),
                MethodDescriptorUtils.getFullDescriptor(method)
        );
        final boolean isEP = isEntryPoint(context, method);
        CtMethod ctMethod = getClassPool().getMethod(method);
        LogUtils.addCostTimeCode(ctMethod, (stVar, etVar, endBlock) -> {
            endBlock.append(loggerExpr)
                    .append(".log(")
                    .append(ParamValueUtils.convertToString(logKey))
                    .append(", ")
                    .append(type)
                    .append(", (int) ")
                    .append(LogUtils.newCostTimegExpr(stVar, etVar))
                    .append(");\n");

            if (isEP) {
                endBlock.append(loggerExpr)
                        .append(".commit(")
                        .append(ParamValueUtils.convertToString(logKey))
                        .append(");\n");
            }
        });
        if (isEP)
            ctMethod.insertAfter(loggerExpr + ".rollback();", true);
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
