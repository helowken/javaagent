package agent.builtin.transformer;

import agent.base.utils.StringParser;
import agent.base.utils.Utils;
import agent.builtin.transformer.utils.DefaultValueConverter;
import agent.builtin.transformer.utils.TraceItem;
import agent.builtin.transformer.utils.ValueConverter;
import agent.common.utils.JSONUtils;
import agent.server.transform.impl.CallChainTransformer;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.utils.log.LogMgr;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_ON_AFTER;

public class TraceInvokeTransformer extends CallChainTransformer {
    public static final String REG_KEY = "@traceMethod";
    private static final String KEY_OUTPUT_FORMAT = "outputFormat";
    private static final String KEY_CONVERTER_CLASS = "printerClass";
    private static final String KEY_CONTENT = "content";
    private static final String DEFAULT_OUTPUT_FORMAT = StringParser.getKey(KEY_CONTENT);

    private ValueConverter valueConverter;

    @Override
    protected String newLogKey(Map<String, Object> logConf) {
        Map<String, Object> newLogConf = new HashMap<>(logConf);
        newLogConf.put(KEY_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        return regLogText(
                newLogConf,
                Collections.emptyMap()
        );
    }

    @Override
    protected void doSetConfig(Map<String, Object> config) throws Exception {
        String className = Utils.getConfigValue(config, KEY_CONVERTER_CLASS);
        Class<? extends ValueConverter> clazz = Utils.isBlank(className) ?
                DefaultValueConverter.class :
                findClass(className);
        valueConverter = clazz.newInstance();
        super.doSetConfig(config);
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

    private static class Config extends CallChainTimeConfig<SelfInvokeInfo> {

        @Override
        protected SelfInvokeInfo newData(Object[] args, Class<?>[] argTypes, DestInvoke destInvoke, Object[] otherArgs) {
            return new SelfInvokeInfo(args, argTypes);
        }

        @Override
        protected SelfInvokeInfo processOnReturning(SelfInvokeInfo data, Object returnValue, Class<?> returnType, DestInvoke destInvoke, Object[] otherArgs) {
            data.returnType = returnType;
            data.returnValue = returnValue;
            return super.processOnReturning(data, returnValue, returnType, destInvoke, otherArgs);
        }

        @Override
        protected void processOnCompleted(List<SelfInvokeInfo> completed, DestInvoke destInvoke, Object[] otherArgs) {
            final String logKey = Utils.getArgValue(otherArgs, 0);
            ValueConverter valueConverter = Utils.getArgValue(otherArgs, 1);
            String content = JSONUtils.writeAsString(
                    completed.stream()
                            .map(
                                    item -> convert(item, valueConverter)
                            )
                            .collect(
                                    Collectors.toList()
                            )
            );
            LogMgr.logText(logKey, content);
        }

        private TraceItem convert(SelfInvokeInfo item, ValueConverter valueConverter) {
            TraceItem traceItem = new TraceItem();
            traceItem.setId(item.id);
            traceItem.setParentId(item.parentId);
            traceItem.setInvokeId(item.invokeId);
            traceItem.setStartTime(item.startTime);
            traceItem.setEndTime(item.endTime);

            List<Map<String, Object>> argMaps = new ArrayList<>();
            if (item.argValues != null) {
                for (int i = 0, len = item.argValues.length; i < len; ++i) {
                    argMaps.add(
                            valueConverter.convertArg(i, item.argTypes[i], item.argValues[i])
                    );
                }
            }
            traceItem.setArgs(argMaps);

            if (item.error != null)
                traceItem.setError(
                        valueConverter.convertError(item.error)
                );
            else
                traceItem.setReturnValue(
                        valueConverter.convertReturnValue(
                                item.returnType,
                                item.returnValue
                        )
                );

            return traceItem;
        }
    }

    private static class SelfInvokeInfo extends InvokeTimeItem {
        private final Object[] argValues;
        private final Class<?>[] argTypes;
        private Object returnValue;
        private Class<?> returnType;

        private SelfInvokeInfo(Object[] argValues, Class<?>[] argTypes) {
            this.argValues = argValues;
            this.argTypes = argTypes;
        }
    }
}
