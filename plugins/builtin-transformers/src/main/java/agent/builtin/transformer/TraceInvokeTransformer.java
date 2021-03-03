package agent.builtin.transformer;

import agent.base.utils.Utils;
import agent.builtin.transformer.utils.DefaultValueConverter;
import agent.builtin.transformer.utils.TraceItem;
import agent.builtin.transformer.utils.ValueConverter;
import agent.base.struct.impl.Struct;
import agent.base.struct.impl.StructContext;
import agent.invoke.DestInvoke;
import agent.server.transform.impl.CallChainTransformer;
import agent.server.utils.log.LogConfigParser;
import agent.server.utils.log.LogMgr;

import java.util.*;
import java.util.stream.Collectors;

import static agent.builtin.transformer.utils.TraceItem.TYPE_CATCH;
import static agent.builtin.transformer.utils.TraceItem.TYPE_INVOKE;

public class TraceInvokeTransformer extends CallChainTransformer {
    public static final String REG_KEY = "@traceInvoke";
    private static final StructContext context = new StructContext();

    @Override
    @SuppressWarnings("unchecked")
    protected void doSetConfig(Map<String, Object> config) throws Exception {
        Map<String, Object> overwrite = new HashMap<>();
        overwrite.put(LogConfigParser.CONF_ROLL_FILE, false);
        overwrite.put(LogConfigParser.CONF_NEED_METADATA, true);
        regLog(config, overwrite);
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    private static class Config extends CallChainTimeConfig<TraceInvokeInfo, TraceResult> {

        @Override
        protected TraceInvokeInfo newData(Object[] args, Class<?>[] argTypes, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            return new TraceInvokeInfo(args, argTypes);
        }

        @Override
        protected TraceResult convertTo(TraceInvokeInfo data) {
            return data;
        }

        @Override
        protected TraceResult processOnReturning(TraceInvokeInfo data, Object returnValue, Class<?> returnType, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            data.returnType = returnType;
            data.returnValue = returnValue;
            return super.processOnReturning(data, returnValue, returnType, instanceOrNull, destInvoke, otherArgs);
        }

        @Override
        protected TraceResult processOnCatching(TraceInvokeInfo data, Throwable error, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            TraceCatchInfo result = new TraceCatchInfo();
            result.id = getAroundItem().nextSeq();
            result.parentId = data.id;
            result.invokeId = data.invokeId;
            result.error = error;
            return result;
        }

        @Override
        protected void processOnCompleted(List<TraceResult> completed, Object instanceOrNull, DestInvoke destInvoke, Object[] otherArgs) {
            final String logKey = Utils.getArgValue(otherArgs, 0);
            ValueConverter valueConverter = new DefaultValueConverter();
            Object data = completed.stream()
                    .map(
                            item -> item.convert(valueConverter)
                    )
                    .collect(
                            Collectors.toList()
                    );
            LogMgr.logBinary(
                    logKey,
                    buf -> Struct.serialize(
                            buf,
                            Arrays.asList(
                                    valueConverter.getMetadata(),
                                    data
                            ),
                            context
                    ),
                    true
            );
        }
    }

    private static TraceItem newTraceItem(InvokeItem data, int type) {
        TraceItem traceItem = new TraceItem();
        traceItem.setId(data.id);
        traceItem.setParentId(data.parentId);
        traceItem.setInvokeId(data.invokeId);
        traceItem.setType(type);
        return traceItem;
    }

    interface TraceResult {
        TraceItem convert(ValueConverter converter);
    }

    private static class TraceInvokeInfo extends InvokeTimeItem implements TraceResult {
        private final Object[] argValues;
        private final Class<?>[] argTypes;
        private Object returnValue;
        private Class<?> returnType;

        private TraceInvokeInfo(Object[] argValues, Class<?>[] argTypes) {
            this.argValues = argValues;
            this.argTypes = argTypes;
        }

        @Override
        public TraceItem convert(ValueConverter converter) {
            TraceItem traceItem = newTraceItem(this, TYPE_INVOKE);
            traceItem.setStartTime(this.startTime);
            traceItem.setEndTime(this.endTime);

            List<Map<String, Object>> argMaps = new ArrayList<>();
            if (this.argValues != null) {
                for (int i = 0, len = this.argValues.length; i < len; ++i) {
                    argMaps.add(
                            converter.convertArg(i, this.argTypes[i], this.argValues[i])
                    );
                }
            }
            traceItem.setArgs(argMaps);

            if (this.error != null)
                traceItem.setError(
                        converter.convertError(this.error)
                );
            else
                traceItem.setReturnValue(
                        converter.convertReturnValue(
                                this.returnType,
                                this.returnValue
                        )
                );

            return traceItem;
        }
    }

    private static class TraceCatchInfo extends InvokeItem implements TraceResult {
        private Throwable error;

        @Override
        public TraceItem convert(ValueConverter converter) {
            TraceItem traceItem = newTraceItem(this, TYPE_CATCH);
            traceItem.setError(
                    converter.convertError(this.error)
            );
            return traceItem;
        }
    }
}
