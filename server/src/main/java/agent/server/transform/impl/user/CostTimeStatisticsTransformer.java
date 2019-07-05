package agent.server.transform.impl.user;

import agent.server.transform.TransformMgr;
import agent.server.transform.config.ClassConfig;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.MethodFinder;
import agent.server.transform.impl.TargetClassConfig;
import agent.server.transform.impl.TransformerInfo;
import agent.server.transform.impl.user.utils.CostTimeLogger;
import agent.server.transform.impl.user.utils.LogTimeUtils;
import agent.server.utils.JSONUtils;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;
import com.fasterxml.jackson.core.type.TypeReference;
import javassist.CtClass;
import javassist.CtMethod;

import java.util.*;

@SuppressWarnings("unchecked")
public class CostTimeStatisticsTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "costTimeStatistics";
    private static final String KEY_ENTRY_POINT = "entryPoint";

    private String logKey;
    private Set<String> entryPoints = new HashSet<>();

    @Override
    protected void doSetConfig(Map<String, Object> config) throws Exception {
        logKey = LogMgr.regBinary(config, Collections.EMPTY_MAP);
        configEntryPoint(config);
    }

    private void configEntryPoint(Map<String, Object> config) throws Exception {
        List<Map<String, Object>> entryPointConfig = (List) Optional.ofNullable(config.get(KEY_ENTRY_POINT))
                .orElseThrow(() -> new RuntimeException("No entry point found in config."));
        List<ClassConfig> classConfigList = JSONUtils.convert(entryPointConfig, new TypeReference<List<ClassConfig>>() {
        });
        TransformerInfo transformerInfo = getTransformerInfo();
        List<TargetClassConfig> targetClassConfigList = TransformMgr.getInstance().convert(transformerInfo.getContext(), classConfigList);
        for (TargetClassConfig targetClassConfig : targetClassConfigList) {
            MethodFinder.getInstance().consume(targetClassConfig, result ->
                    result.methodList.forEach(method ->
                            entryPoints.add(
                                    getEntryPoint(result.ctClass, method)
                            )
                    )
            );
        }
    }

    private String getEntryPoint(CtClass ctClass, CtMethod ctMethod) {
        return ctClass.getName() + "." + ctMethod.getName() + ctMethod.getSignature();
    }

    private boolean isEntryPoint(CtClass ctClass, CtMethod ctMethod) {
        return entryPoints.contains(
                getEntryPoint(ctClass, ctMethod)
        );
    }

    @Override
    protected void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {
        String loggerExpr = CostTimeLogger.class.getName() + ".getInstance()";
        short type = CostTimeLogger.getInstance().reg(
                getEntryPoint(ctClass, ctMethod)
        );
        LogTimeUtils.addCostTimeCode(ctMethod, (stVar, etVar, endBlock) -> {
            endBlock.append(loggerExpr)
                    .append(".log(")
                    .append("(short) ")
                    .append(type)
                    .append(", (int) ")
                    .append(LogTimeUtils.newCostTimegExpr(stVar, etVar))
                    .append(");\n");

            if (isEntryPoint(ctClass, ctMethod)) {
                endBlock.append(loggerExpr)
                        .append(".commit(")
                        .append(ParamValueUtils.convertToString(logKey))
                        .append(");\n");
            }
        });
    }

}
