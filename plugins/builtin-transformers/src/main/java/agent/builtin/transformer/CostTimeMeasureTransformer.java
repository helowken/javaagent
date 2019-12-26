package agent.builtin.transformer;

import agent.base.utils.IndentUtils;
import agent.base.utils.StringParser;
import agent.base.utils.Utils;
import agent.server.transform.impl.CallChainTransformer;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.text.TextLogConfigParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CostTimeMeasureTransformer extends CallChainTransformer {
    public static final String REG_KEY = "sys_costTimeMeasure";
    private static final String KEY_COST_TIME = "costTime";
    private static final String DEFAULT_OUTPUT_FORMAT = StringParser.getKey(ParamValueUtils.KEY_METHOD) +
            " cost time is: " + StringParser.getKey(KEY_COST_TIME) + "ms";

    @Override
    protected String newLogKey(Map<String, Object> config) {
        Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put(TextLogConfigParser.CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        return regLogText(config, defaultValueMap);
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    private static class Config extends CallChainTimeConfig<SelfInvokeItem> {
        @Override
        protected SelfInvokeItem newData(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs) {
            return new SelfInvokeItem();
        }

        @Override
        protected SelfInvokeItem processOnReturning(SelfInvokeItem data, Object returnValue, Class<?> returnType, DestInvoke destInvoke, Object[] otherArgs) {
            data.count = getAroundItem().size();
            return super.processOnReturning(data, returnValue, returnType, destInvoke, otherArgs);
        }

        @Override
        protected void processOnCompleted(List<SelfInvokeItem> completed, DestInvoke destInvoke, Object[] otherArgs) {
            final String logKey = Utils.getArgValue(otherArgs, 0);
            completed.forEach(
                    item -> LogMgr.logText(
                            logKey,
                            ParamValueUtils.newParamValueMap(
                                    destInvoke.getDeclaringClass().getName(),
                                    IndentUtils.getIndent(item.count) + destInvoke.toString(),
                                    new Object[]{
                                            KEY_COST_TIME,
                                            item.endTime - item.startTime
                                    }
                            )
                    )
            );
        }
    }

    private static class SelfInvokeItem extends InvokeTimeItem {
        private int count;
    }
}
