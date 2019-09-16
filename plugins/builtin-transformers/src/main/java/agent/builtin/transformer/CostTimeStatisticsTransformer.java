package agent.builtin.transformer;

import agent.builtin.transformer.utils.CostTimeLogger;
import agent.builtin.transformer.utils.LogUtils;
import agent.common.utils.JSONUtils;
import agent.server.transform.TransformMgr;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.TargetClassConfig;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.impl.utils.ClassPoolUtils;
import agent.server.transform.impl.utils.MethodFinder;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogConfig;
import agent.server.utils.log.LogMgr;
import com.fasterxml.jackson.core.type.TypeReference;
import javassist.CtClass;
import javassist.CtMethod;

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
        List<Map<String, Object>> entryPointConfig = (List) Optional.ofNullable(config.get(KEY_ENTRY_POINT))
                .orElseThrow(() -> new RuntimeException("No entry point found in config."));
        List<ClassConfig> classConfigList = JSONUtils.convert(entryPointConfig, new TypeReference<List<ClassConfig>>() {
        });
        TransformerInfo transformerInfo = getTransformerInfo();
        List<TargetClassConfig> targetClassConfigList = TransformMgr.getInstance().convert(transformerInfo.getContext(), classConfigList);
        ClassPoolUtils.exec((cp, classPathRecorder) -> {
            for (TargetClassConfig targetClassConfig : targetClassConfigList) {
                classPathRecorder.add(targetClassConfig.targetClass);
                MethodFinder.getInstance().consume(
                        cp,
                        targetClassConfig,
                        result -> result.methodList.forEach(method ->
                                entryPoints.add(
                                        getEntryPoint(transformerInfo.getContext(), result.ctClass, method)
                                )
                        )
                );
            }
        });
        LogConfig logConfig = LogMgr.getBinaryLogConfig(logKey);
        CostTimeLogger.getInstance().regOutputPath(transformerInfo.getContext(), logConfig.getOutputPath());
    }

    private String getEntryPoint(String context, CtClass ctClass, CtMethod ctMethod) {
        return context + ":" + ctClass.getName() + "." + ctMethod.getName() + ctMethod.getSignature();
    }

    private boolean isEntryPoint(String context, CtClass ctClass, CtMethod ctMethod) {
        return entryPoints.contains(
                getEntryPoint(context, ctClass, ctMethod)
        );
    }

    @Override
    protected void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {
        String context = getTransformerInfo().getContext();
        String loggerExpr = CostTimeLogger.class.getName() + ".getInstance()";
        int type = CostTimeLogger.getInstance().reg(context, ctClass.getName(), ctMethod.getName() + ctMethod.getSignature());
        final boolean isEP = isEntryPoint(context, ctClass, ctMethod);
        LogUtils.addCostTimeCode(ctMethod, (stVar, etVar, endBlock) -> {
            endBlock.append(loggerExpr)
                    .append(".log(")
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
