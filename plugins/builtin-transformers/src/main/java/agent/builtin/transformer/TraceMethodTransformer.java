package agent.builtin.transformer;

import agent.base.utils.StringParser;
import agent.base.utils.Utils;
import agent.builtin.transformer.utils.DefaultMethodPrinter;
import agent.builtin.transformer.utils.MethodLogger;
import agent.server.transform.impl.ProxyAnnotationConfigTransformer;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.text.TextLogConfigParser;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TraceMethodTransformer extends ProxyAnnotationConfigTransformer {
    public static final String REG_KEY = "sys_traceMethod";
    private static final String KEY_CONTENT = "content";
    private static final String KEY_PRINTER_CLASS = "printerClass";
    private static final String DEFAULT_OUTPUT_FORMAT = StringParser.getKey(ParamValueUtils.KEY_METHOD) +
            ":\n" + StringParser.getKey(KEY_CONTENT);

    private String logKey;
    private String printerClass;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        printerClass = Optional.ofNullable(
                Utils.blankToNull(
                        (String) config.get(KEY_PRINTER_CLASS)
                )
        ).orElse(
                DefaultMethodPrinter.class.getName()
        );
        Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put(TextLogConfigParser.CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        logKey = LogMgr.regText(config, defaultValueMap);
    }

    @Override
    protected void transformDestInvoke(DestInvoke destInvoke) throws Exception {
        String methodLoggerClassName = MethodLogger.class.getName();
        String methodLoggerVar = "methodInfo";
//        ctMethod.addLocalVariable(
//                methodLoggerVar,
//                getClassPool().get(methodLoggerClassName)
//        );
//
//        String block = methodLoggerVar + " = new " + methodLoggerClassName + "(\"" + printerClass + "\");"
//                + methodLoggerVar + ".printArgs($args, $sig);";
//        ctMethod.insertBefore(block);
//
//        StringBuilder endBlock = new StringBuilder(methodLoggerVar + ".printReturnValue(($w) $_, $type);");
//        String pvsCode = ParamValueUtils.genCode(
//                method.getDeclaringClass().getName(),
//                method.getName(),
//                KEY_CONTENT,
//                new Expr(methodLoggerVar + ".getContent()"));
//        LogUtils.addLogTextCode(endBlock, logKey, pvsCode);
//        ctMethod.insertAfter(endBlock.toString());
    }

    @Override
    protected Object getInstanceForMethod(Method method) {
        return null;
    }

    @Override
    protected Set<Class<?>> getAnnotationClasses() {
        return null;
    }

    @Override
    protected Object[] newOtherArgs(DestInvoke destInvoke, Method anntMethod, int argsHint) {
        return new Object[0];
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    protected Object[] newOtherArgs(Method srcMethod, Method anntMethod, int argsHint) {
        return new Object[0];
    }

//    static class TraceMethodConfig extends ProxyAnnotationConfig {
//
//    }
}
