package agent.builtin.transformer;

import agent.base.utils.Utils;
import agent.invoke.DestInvoke;
import agent.server.transform.impl.CallChainTransformer;
import agent.server.utils.log.LogConfigParser;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConsumedTimeStatisticsTransformer extends CallChainTransformer {
    public static final String REG_KEY = "@consumedTimeStat";

    @Override
    @SuppressWarnings("unchecked")
    protected void doSetConfig(Map<String, Object> config) throws Exception {
        regLog(
                config,
                Collections.singletonMap(
                        LogConfigParser.CONF_NEED_METADATA,
                        true
                )
        );
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }


    private static class Config extends CallChainTimeConfig<InvokeTimeItem, InvokeTimeItem> {
        @Override
        protected InvokeTimeItem newData(Object[] args, Class<?>[] argTypes, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            return new InvokeTimeItem();
        }

        @Override
        protected void processOnCompleted(List<InvokeTimeItem> completed, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            final String logKey = Utils.getArgValue(otherArgs, 0);
            BinaryLogItem logItem = BinaryLogItemPool.get(logKey);
            logItem.putInt(
                    completed.size()
            );
            completed.forEach(
                    item -> {
                        logItem.putInt(item.id);
                        logItem.putInt(item.parentId);
                        logItem.putInt(item.invokeId);
                        logItem.putInt((int) (item.endTime - item.startTime));
                        logItem.put((byte) (item.error != null ? 1 : 0));
                    }
            );
            LogMgr.logBinary(logKey, logItem);
        }

        @Override
        protected InvokeTimeItem convertTo(InvokeTimeItem data) {
            return data;
        }
    }

}
