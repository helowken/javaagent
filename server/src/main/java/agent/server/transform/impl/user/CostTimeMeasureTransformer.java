package agent.server.transform.impl.user;

import agent.base.utils.StringParser;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.user.utils.LogTimeUtils;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.text.TextLogConfigParser;
import javassist.CtClass;
import javassist.CtMethod;

import java.util.HashMap;
import java.util.Map;

public class CostTimeMeasureTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "costTimeMeasure";
    private static final String KEY_COST_TIME = "costTime";
    private static final String DEFAULT_OUTPUT_FORMAT =
            StringParser.getKey(ParamValueUtils.KEY_CLASS) + "."
                    + StringParser.getKey(ParamValueUtils.KEY_METHOD) +
                    " cost time is: " + StringParser.getKey(KEY_COST_TIME) + "ms";

    private String logKey;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put(TextLogConfigParser.CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        logKey = LogMgr.regText(config, defaultValueMap);
    }

    @Override
    public void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {
        LogTimeUtils.addCostTimeCode(ctMethod, (stVar, etVar, endBlock) -> {
            String pvsCode = ParamValueUtils.genCode(
                    ctClass.getName(),
                    ctMethod.getName(),
                    KEY_COST_TIME,
                    LogTimeUtils.newCostTimeStringExpr(stVar, etVar)
            );
            endBlock.append(LogMgr.class.getName())
                    .append(".logText(")
                    .append(ParamValueUtils.convertToString(logKey))
                    .append(", ")
                    .append(pvsCode)
                    .append(");");
        });
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
