package agent.builtin.transformer;

import agent.base.utils.IndentUtils;
import agent.base.utils.StringParser;
import agent.base.utils.Utils;
import agent.server.transform.impl.ProxyAnnotationConfig;
import agent.server.transform.impl.ProxyAnnotationConfigTransformer;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.text.TextLogConfigParser;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_ON_AFTER;

public class CostTimeMeasureTransformer extends ProxyAnnotationConfigTransformer {
    public static final String REG_KEY = "sys_costTimeMeasure";
    private static final String KEY_COST_TIME = "costTime";
    private static final String DEFAULT_OUTPUT_FORMAT = StringParser.getKey(ParamValueUtils.KEY_METHOD) +
            " cost time is: " + StringParser.getKey(KEY_COST_TIME) + "ms";

    private String logKey;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put(TextLogConfigParser.CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        logKey = regLogText(config, defaultValueMap);
    }

    @Override
    protected Object[] newOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint) {
        if (ARGS_ON_AFTER == argsHint)
            return new Object[]{
                    logKey
            };
        return null;
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    static class CostTimeMeasureConfig extends ProxyAnnotationConfig<Long, Map<String, Object>> {
        @Override
        protected Long newDataOnBefore(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs) {
            return System.currentTimeMillis();
        }

        @Override
        protected Map<String, Object> processOnReturning(Long data, Object returnValue, Class<?> returnType, DestInvoke destInvoke, Object[] otherArgs) {
            long et = System.currentTimeMillis();
            return ParamValueUtils.newParamValueMap(
                    destInvoke.getDeclaringClass().getName(),
                    IndentUtils.getIndent(
                            getAroundItem().size()
                    ) + destInvoke.toString(),
                    new Object[]{
                            KEY_COST_TIME,
                            et - data
                    }
            );
        }

        @Override
        protected Map<String, Object> processOnThrowing(Long data, Throwable error, DestInvoke destInvoke, Object[] otherArgs) {
            return null;
        }

        @Override
        protected void processOnAfter(DestInvoke destInvoke, Object[] otherArgs) {

        }

        @Override
        protected void processOnCompleted(List<Map<String, Object>> completed, DestInvoke destInvoke, Object[] otherArgs) {
            final String logKey = Utils.getArgValue(otherArgs, 0);
            completed.forEach(
                    params -> LogMgr.logText(logKey, params)
            );
        }

    }
}
