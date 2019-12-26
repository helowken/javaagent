package agent.builtin.transformer;

import agent.base.utils.Utils;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.ProxyAnnotationConfig;
import agent.server.transform.impl.ProxyAnnotationConfigTransformer;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_ON_AFTER;
import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_ON_BEFORE;

@SuppressWarnings("unchecked")
public class CostTimeStatisticsTransformer extends ProxyAnnotationConfigTransformer {
    public static final String REG_KEY = "sys_costTimeStatistics";

    private String logKey;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        logKey = regLogBinary(config, Collections.EMPTY_MAP);
    }

    @Override
    protected Object[] newOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint) {
        switch (argsHint) {
            case ARGS_ON_BEFORE:
                return new Object[]{
                        DestInvokeIdRegistry.getInstance().get(destInvoke)
                };
            case ARGS_ON_AFTER:
                return new Object[]{
                        logKey
                };
        }
        return null;
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }


    static class CostTimeStatisticsConfig extends ProxyAnnotationConfig<MethodItem, MethodItem> {
        @Override
        protected MethodItem newDataOnBefore(Object[] args, Class[] argTypes, DestInvoke destInvoke, Object[] otherArgs) {
            int invokeId = Utils.getArgValue(otherArgs, 0);
            MethodItem parentItem = getAroundItem().peek();
            int parentMethodId = parentItem == null ? -1 : parentItem.invokeId;
            return new MethodItem(
                    parentMethodId,
                    invokeId,
                    System.currentTimeMillis()
            );
        }

        @Override
        protected MethodItem processOnReturning(MethodItem data, Object returnValue, Class<?> returnType, DestInvoke destInvoke, Object[] otherArgs) {
            data.error = false;
            data.endTime = System.currentTimeMillis();
            return data;
        }

        @Override
        protected MethodItem processOnThrowing(MethodItem data, Throwable error, DestInvoke destInvoke, Object[] otherArgs) {
            data.error = true;
            data.endTime = System.currentTimeMillis();
            return data;
        }

        @Override
        protected void processOnAfter(DestInvoke destInvoke, Object[] otherArgs) {
        }

        @Override
        protected void processOnCompleted(List<MethodItem> completed, DestInvoke destInvoke, Object[] otherArgs) {
            final String logKey = Utils.getArgValue(otherArgs, 0);
            BinaryLogItem logItem = BinaryLogItemPool.get(logKey);
            logItem.putInt(
                    completed.size()
            );
            completed.forEach(
                    item -> {
                        logItem.putInt(item.parentMethodId);
                        logItem.putInt(item.invokeId);
                        logItem.putInt((int) (item.endTime - item.startTime));
                        logItem.put((byte) (item.error ? 1 : 0));
                    }
            );
            LogMgr.logBinary(logKey, logItem);
        }
    }

    private static class MethodItem {
        private final int parentMethodId;
        private final int invokeId;
        private final long startTime;
        private long endTime;
        private boolean error;

        private MethodItem(int parentMethodId, int invokeId, long startTime) {
            this.parentMethodId = parentMethodId;
            this.invokeId = invokeId;
            this.startTime = startTime;
        }
    }

}
