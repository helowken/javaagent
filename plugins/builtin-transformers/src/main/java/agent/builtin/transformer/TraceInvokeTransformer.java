package agent.builtin.transformer;

import agent.base.utils.Utils;
import agent.builtin.transformer.utils.DefaultValueConverter;
import agent.builtin.transformer.utils.TraceItem;
import agent.builtin.transformer.utils.ValueConverter;
import agent.common.utils.JsonUtils;
import agent.invoke.DestInvoke;
import agent.server.transform.impl.CallChainTransformer;
import agent.server.utils.log.LogMgr;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static agent.builtin.transformer.utils.TraceItem.TYPE_CATCH;
import static agent.builtin.transformer.utils.TraceItem.TYPE_INVOKE;
import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_ON_AFTER;

public class TraceInvokeTransformer extends CallChainTransformer {
    public static final String REG_KEY = "@traceInvoke";

    private static final ValueConverter valueConverter = new DefaultValueConverter();

    @Override
    protected String newLogKey(Map<String, Object> logConf) {
        return regLogText(
                logConf,
                Collections.emptyMap()
        );
    }

    @Override
    protected Object[] newOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint) {
        if (argsHint == ARGS_ON_AFTER)
            return new Object[]{
                    logKey,
                    valueConverter
            };
        return super.newOtherArgs(destInvoke, anntMethod, argsHint);
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    private static class Config extends CallChainTimeConfig<TraceInvokeInfo, TraceResult> {

        @Override
        protected TraceInvokeInfo newData(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs) {
            return new TraceInvokeInfo(args, argTypes);
        }

        @Override
        protected TraceResult convertTo(TraceInvokeInfo data) {
            return data;
        }

        @Override
        protected TraceResult processOnReturning(TraceInvokeInfo data, Object returnValue, Class<?> returnType, DestInvoke destInvoke, Object[] otherArgs) {
            data.returnType = returnType;
            data.returnValue = returnValue;
            return super.processOnReturning(data, returnValue, returnType, destInvoke, otherArgs);
        }

        @Override
        protected TraceResult processOnCatching(TraceInvokeInfo data, Throwable error, DestInvoke destInvoke, Object[] otherArgs) {
            TraceCatchInfo result = new TraceCatchInfo();
            result.id = getAroundItem().nextSeq();
            result.parentId = data.id;
            result.invokeId = data.invokeId;
            result.error = error;
            return result;
        }

        @Override
        protected void processOnCompleted(List<TraceResult> completed, DestInvoke destInvoke, Object[] otherArgs) {
            final String logKey = Utils.getArgValue(otherArgs, 0);
            ValueConverter valueConverter = Utils.getArgValue(otherArgs, 1);
            String content = JsonUtils.writeAsString(
                    completed.stream()
                            .map(
                                    item -> item.convert(valueConverter)
                            )
                            .collect(
                                    Collectors.toList()
                            )
            );
            LogMgr.logText(logKey, content + '\n');
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
                            valueConverter.convertArg(i, this.argTypes[i], this.argValues[i])
                    );
                }
            }
            traceItem.setArgs(argMaps);

            if (this.error != null)
                traceItem.setError(
                        valueConverter.convertError(this.error)
                );
            else
                traceItem.setReturnValue(
                        valueConverter.convertReturnValue(
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
                    valueConverter.convertError(this.error)
            );
            return traceItem;
        }
    }
}
