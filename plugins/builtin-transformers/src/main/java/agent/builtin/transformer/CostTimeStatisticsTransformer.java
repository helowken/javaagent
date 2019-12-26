package agent.builtin.transformer;

import agent.base.utils.Utils;
import agent.server.transform.impl.CallChainTransformer;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class CostTimeStatisticsTransformer extends CallChainTransformer {
    public static final String REG_KEY = "sys_costTimeStat";

    @Override
    protected String newLogKey(Map<String, Object> config) {
        return regLogBinary(config, Collections.EMPTY_MAP);
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }


    private static class Config extends CallChainTimeConfig<InvokeTimeItem> {
        @Override
        protected InvokeTimeItem newData(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs) {
            return new InvokeTimeItem();
        }

        @Override
        protected void processOnCompleted(List<InvokeTimeItem> completed, DestInvoke destInvoke, Object[] otherArgs) {
            final String logKey = Utils.getArgValue(otherArgs, 0);
            BinaryLogItem logItem = BinaryLogItemPool.get(logKey);
            logItem.putInt(
                    completed.size()
            );
            completed.forEach(
                    item -> {
                        logItem.putInt(item.parentInvokeId);
                        logItem.putInt(item.invokeId);
                        logItem.putInt((int) (item.endTime - item.startTime));
                        logItem.put((byte) (item.error != null ? 1 : 0));
                    }
            );
            LogMgr.logBinary(logKey, logItem);
        }
    }

}
