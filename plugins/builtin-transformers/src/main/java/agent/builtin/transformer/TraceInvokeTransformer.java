package agent.builtin.transformer;

import agent.base.utils.StringParser;
import agent.base.utils.Utils;
import agent.builtin.transformer.utils.DefaultMethodPrinter;
import agent.builtin.transformer.utils.ValueConverter;
import agent.server.transform.impl.CallChainTransformer;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;

import java.lang.reflect.Method;
import java.util.*;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_ON_AFTER;

public class TraceInvokeTransformer extends CallChainTransformer {
    public static final String REG_KEY = "sys_traceMethod";
    private static final String KEY_OUTPUT_FORMAT = "outputFormat";
    private static final String KEY_CONVERTER_CLASS = "printerClass";
    private static final String KEY_CONTENT = "content";
    private static final String DEFAULT_OUTPUT_FORMAT = StringParser.getKey(KEY_CONTENT);

    private ValueConverter valueConverter;

    @Override
    protected String newLogKey(Map<String, Object> config) {
        return regLogText(
                config,
                Collections.singletonMap(
                        KEY_OUTPUT_FORMAT,
                        DEFAULT_OUTPUT_FORMAT
                )
        );
    }

    @Override
    protected void doSetConfig(Map<String, Object> config) throws Exception {
        String className = Utils.getConfigValue(config, KEY_CONVERTER_CLASS);
        Class<? extends ValueConverter> clazz = Utils.isBlank(className) ?
                DefaultMethodPrinter.class :
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
        private static final String KEY_ID = "id";
        private static final String KEY_PARENT = "parent";
        private static final String KEY_ARGS = "args";
        private static final String KEY_RETURN_VALUE = "returnValue";
        private static final String KEY_ERROR = "error";

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
            completed.forEach(
                    item -> LogMgr.logText(
                            logKey,
                            ParamValueUtils.newParamValueMap(
                                    KEY_CONTENT,
                                    convert(item, valueConverter)
                            )
                    )
            );
        }

        private Map<String, Object> convert(SelfInvokeInfo item, ValueConverter valueConverter) {
            Map<String, Object> rsMap = new HashMap<>();
            List<Map<String, Object>> argMaps = new ArrayList<>();
            Object[] argValues = item.argValues;
            if (argValues == null)
                argValues = new Object[0];

            for (int i = 0, len = argValues.length; i < len; ++i) {
                argMaps.add(
                        valueConverter.convertArg(i, item.argTypes[i], item.argValues[i])
                );
            }

            rsMap.put(KEY_ID, item.invokeId);
            rsMap.put(KEY_PARENT, item.parentInvokeId);
            rsMap.put(KEY_ARGS, argMaps);
            if (item.error != null)
                rsMap.put(
                        KEY_ERROR,
                        valueConverter.convertError(item.error)
                );
            else
                rsMap.put(
                        KEY_RETURN_VALUE,
                        valueConverter.convertReturnValue(
                                item.returnType,
                                item.returnValue
                        )
                );
            return rsMap;
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
