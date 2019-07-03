package agent.server.transform.impl.user;

import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.utils.IOLogger;
import agent.base.utils.Logger;
import agent.server.utils.ParamValueUtils;
import agent.base.utils.StringParser;
import javassist.CtClass;
import javassist.CtMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static agent.server.utils.IOLogger.IOLogConfig;
import static agent.server.utils.IOLogger.IOLogConfigParser;
import static agent.server.utils.ParamValueUtils.Expr;

public class CostTimeMeasureTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "costTimeMeasure";
    private static final Logger logger = Logger.getLogger(CostTimeMeasureTransformer.class);
    private static final String KEY_COST_TIME = "costTime";
    private static final String DEFAULT_OUTPUT_FORMAT =
            StringParser.getKey(ParamValueUtils.KEY_CLASS) + "."
                    + StringParser.getKey(ParamValueUtils.KEY_METHOD) +
                    " cost time is: " + StringParser.getKey(KEY_COST_TIME) + "ms";

    private final String uuid = UUID.randomUUID().toString();

    public CostTimeMeasureTransformer() {
        logger.debug("ClassLoader: {}", getClass().getClassLoader());
    }

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        Map<String, Object> defaultValueMap = new HashMap<>();
        defaultValueMap.put(IOLogConfigParser.CONF_OUTPUT_FORMAT, DEFAULT_OUTPUT_FORMAT);
        IOLogConfig logConfig = IOLogConfigParser.parse(config, defaultValueMap);
        IOLogger.getInstance().reg(uuid, logConfig);
    }

    @Override
    public void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {
        addTimeMeasureCode(ctClass.getName(), ctMethod);
    }

    private void addTimeMeasureCode(String targetClassName, CtMethod ctMethod) throws Exception {
        final String stVar = "startTime";
        final String etVar = "endTime";
        ctMethod.addLocalVariable(stVar, CtClass.longType);
        ctMethod.insertBefore(stVar + " = System.currentTimeMillis();");

        ctMethod.addLocalVariable(etVar, CtClass.longType);
        String endBlock = etVar + " = System.currentTimeMillis();\n";
        String pvsCode = ParamValueUtils.genCode(targetClassName, ctMethod.getName(), KEY_COST_TIME,
                new Expr("\"\" + (" + etVar + " - " + stVar + ")"));
        endBlock += IOLogger.class.getName() + ".getInstance().logTime("
                + ParamValueUtils.convertToString(uuid)
                + ", "
                + pvsCode
                + ");";
//        logger.debug("End block: {}", endBlock);
        ctMethod.insertAfter(endBlock);
    }
}
