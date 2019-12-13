package agent.builtin.transformer;

import agent.base.utils.StringParser;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.tools.asm.ProxyCallChain;
import agent.server.transform.tools.asm.ProxyCallInfo;
import agent.server.transform.tools.asm.ProxyRegInfo;
import agent.server.utils.ParamValueUtils;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.text.TextLogConfigParser;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static agent.server.transform.tools.asm.ProxyArgsMask.DEFAULT_AROUND;
import static agent.server.transform.tools.asm.ProxyArgsMask.MASK_INVOKE_METHOD;

public class CostTimeMeasureTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "sys_costTimeMeasure";
    private static final String KEY_COST_TIME = "costTime";
    private static final String DEFAULT_OUTPUT_FORMAT =
            StringParser.getKey(ParamValueUtils.KEY_CLASS) + "#"
                    + StringParser.getKey(ParamValueUtils.KEY_METHOD) +
                    " cost time is: " + StringParser.getKey(KEY_COST_TIME) + "ms";

    private String logKey;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put(TextLogConfigParser.CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        logKey = LogMgr.regText(config, defaultValueMap);
    }

    @Override
    protected void transformMethod(Method method) throws Exception {
        addRegInfo(
                new ProxyRegInfo(method).addAround(
                        new ProxyCallInfo(
                                findSelfMethod("logCostTime"),
                                DEFAULT_AROUND | MASK_INVOKE_METHOD,
                                new Object[]{logKey}
                        )
                )
        );
    }

    private static void logCostTime(ProxyCallChain callChain, Method srcMethod, final String logKey) {
        long st = System.currentTimeMillis();
        callChain.process();
        long et = System.currentTimeMillis();
        LogMgr.logText(
                logKey,
                ParamValueUtils.newParamValueMap(
                        srcMethod.getDeclaringClass().getName(),
                        srcMethod.getName(),
                        new Object[]{
                                KEY_COST_TIME,
                                et - st
                        }
                )
        );
    }

//    @Override
//    public void transformMethod(Method method) throws Exception {
//        LogUtils.addCostTimeCode(
//                getClassPool().getMethod(method),
//                (stVar, etVar, endBlock) -> {
//                    String pvsCode = ParamValueUtils.genCode(
//                            method.getDeclaringClass().getName(),
//                            method.getName(),
//                            KEY_COST_TIME,
//                            LogUtils.newCostTimeStringExpr(stVar, etVar)
//                    );
//                    LogUtils.addLogTextCode(endBlock, logKey, pvsCode);
//                }
//        );
//    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
