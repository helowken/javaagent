package agent.builtin.transformer;

import agent.base.utils.StringParser;
import agent.base.utils.Utils;
import agent.server.transform.impl.ProxyAnnotationConfig;
import agent.server.transform.impl.ProxyAnnotationConfigTransformer;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.text.TextLogConfigParser;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_ON_RETURNING;

public class CostTimeMeasureTransformer extends ProxyAnnotationConfigTransformer {
    public static final String REG_KEY = "sys_costTimeMeasure";
    private static final String KEY_COST_TIME = "costTime";
    private static final String DEFAULT_OUTPUT_FORMAT = StringParser.getKey(ParamValueUtils.KEY_METHOD) +
            " cost time is: " + StringParser.getKey(KEY_COST_TIME) + "ms";
    private static final CostTimeMeasureConfig measureConfig = new CostTimeMeasureConfig();

    private String logKey;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put(TextLogConfigParser.CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        logKey = LogMgr.regText(config, defaultValueMap);
    }

    @Override
    protected Set<Class<?>> getAnnotationClasses() {
        return Collections.singleton(CostTimeMeasureConfig.class);
    }

    @Override
    protected Object[] newOtherArgs(Method srcMethod, Method anntMethod, int argsHint) {
        if (ARGS_ON_RETURNING == argsHint)
            return new Object[]{
                    logKey
            };
        return null;
    }

    @Override
    protected Object getInstanceForMethod(Method method) {
        return measureConfig;
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    static class CostTimeMeasureConfig extends ProxyAnnotationConfig<Long> {

        @Override
        protected Long newData(Node<Long> preNode, Object[] args, Class<?>[] argTypes, Method method, Object[] otherArgs) {
            return System.currentTimeMillis();
        }

        @Override
        protected void processOnReturning(Node<Long> currNode, Object returnValue, Class<?> returnType, Method method, Object[] otherArgs) {
            long et = System.currentTimeMillis();
            long st = currNode.getData();
            String logKey = Utils.getArgValue(otherArgs, 0);
            LogMgr.logText(
                    logKey,
                    ParamValueUtils.newParamValueMap(
                            method.getDeclaringClass().getName(),
//                            IndentUtils.getIndent(currNode.size() - 1) + method.toString(),
                            method.toString(),
                            new Object[]{
                                    KEY_COST_TIME,
                                    et - st
                            }
                    )
            );
        }
    }
}
