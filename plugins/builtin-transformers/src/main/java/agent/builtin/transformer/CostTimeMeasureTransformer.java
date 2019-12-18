package agent.builtin.transformer;

import agent.base.utils.Logger;
import agent.base.utils.StringParser;
import agent.base.utils.Utils;
import agent.server.transform.impl.AbstractAnnotationConfigTransformer;
import agent.server.transform.tools.asm.annotation.OnAfter;
import agent.server.transform.tools.asm.annotation.OnBefore;
import agent.server.transform.tools.asm.annotation.OnReturning;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.text.TextLogConfigParser;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static agent.server.transform.tools.asm.ProxyArgsMask.MASK_INVOKE_METHOD;

public class CostTimeMeasureTransformer extends AbstractAnnotationConfigTransformer {
    public static final String REG_KEY = "sys_costTimeMeasure";
    private static final Logger logger = Logger.getLogger(CostTimeMeasureTransformer.class);
    private static final String KEY_COST_TIME = "costTime";
    private static final String DEFAULT_OUTPUT_FORMAT =
            StringParser.getKey(ParamValueUtils.KEY_METHOD) +
                    " cost time is: " + StringParser.getKey(KEY_COST_TIME) + "ms";
    private static final ThreadLocal<LinkedList<Long>> startTimesLocal = new ThreadLocal<>();

    private String logKey;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put(TextLogConfigParser.CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        logKey = LogMgr.regText(config, defaultValueMap);
    }

    @OnBefore
    private static void logCostTimeStart() {
        LinkedList<Long> stList = startTimesLocal.get();
        if (stList == null) {
            stList = new LinkedList<>();
            startTimesLocal.set(stList);
        }
        stList.addFirst(
                System.currentTimeMillis()
        );
    }

    @OnReturning(mask = MASK_INVOKE_METHOD, otherArgsFunc = "getLogKey")
    private static void logCostTimeEnd(Method method, final String logKey) {
        long et = System.currentTimeMillis();
        LinkedList<Long> stList = startTimesLocal.get();
        if (stList == null)
            logger.error("No stList found.");
        else {
            Long st = stList.getFirst();
            LogMgr.logText(
                    logKey,
                    ParamValueUtils.newParamValueMap(
                            method.getDeclaringClass().getName(),
                            method.toString(),
                            new Object[]{
                                    KEY_COST_TIME,
                                    et - st
                            }
                    )
            );
        }
    }

    @OnAfter
    private static void logCostTimeAfter() {
        LinkedList<Long> stList = startTimesLocal.get();
        if (Utils.nonEmpty(stList))
            stList.removeFirst();
        else
            startTimesLocal.remove();
    }

    private String getLogKey() {
        return logKey;
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

}
