package agent.builtin.transformer;

import agent.base.utils.Logger;
import agent.base.utils.StringParser;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.tools.asm.ProxyCallInfo;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.text.TextLogConfigParser;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static agent.server.transform.tools.asm.ProxyArgsMask.MASK_INVOKE_METHOD;
import static agent.server.transform.tools.asm.ProxyArgsMask.MASK_NONE;

public class CostTimeMeasureTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "sys_costTimeMeasure";
    private static final Logger logger = Logger.getLogger(CostTimeMeasureTransformer.class);
    private static final String KEY_COST_TIME = "costTime";
    private static final String DEFAULT_OUTPUT_FORMAT =
            StringParser.getKey(ParamValueUtils.KEY_METHOD) +
                    " cost time is: " + StringParser.getKey(KEY_COST_TIME) + "ms";
    private static final ThreadLocal<Long> startTimeLocal = new ThreadLocal<>();

    private String logKey;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put(TextLogConfigParser.CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        logKey = LogMgr.regText(config, defaultValueMap);
    }

    @Override
    protected void transformMethod(Method method) throws Exception {
        addRegInfo(
                new ProxyRegInfo(method).addBefore(
                        new ProxyCallInfo(
                                findSelfMethod("logCostTimeStart"),
                                MASK_NONE
                        )
                ).addOnReturning(
                        new ProxyCallInfo(
                                findSelfMethod("logCostTimeEnd"),
                                MASK_INVOKE_METHOD,
                                new Object[]{
                                        logKey
                                }
                        )
                ).addAfter(
                        new ProxyCallInfo(
                                findSelfMethod("logCostTimeAfter"),
                                MASK_NONE
                        )
                )
        );
    }

    private static void logCostTimeStart() {
        startTimeLocal.set(
                System.currentTimeMillis()
        );
    }

    private static void logCostTimeEnd(Method method, final String logKey) {
        Long st = startTimeLocal.get();
        if (st == null)
            logger.error("No start time found.");
        else {
            long et = System.currentTimeMillis();
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

    private static void logCostTimeAfter() {
        startTimeLocal.remove();
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
