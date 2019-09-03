package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.server.transform.impl.utils.DynamicConfigRegistry;
import agent.server.transform.impl.utils.DynamicConfigRegistry.DynamicConfigItem;
import javassist.CtClass;
import javassist.CtMethod;

import java.util.Map;

public class DynamicClassTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "$sys_dynamic";
    public static final String KEY_CONFIG = "config";
    private static final Logger logger = Logger.getLogger(DynamicClassTransformer.class);

    private DynamicConfigItem item;
    private String key;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        item = (DynamicConfigItem) config.get(KEY_CONFIG);
        if (item == null)
            throw new RuntimeException("No config item found.");
        key = Utils.sUuid();
        DynamicConfigRegistry.getInstance().reg(key, item);
    }

    @Override
    protected void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {
        String className = getClass().getName();
        String code;
        switch (item.position) {
            case BEFORE:
                code = className + ".runBefore(\"" + key + "\", $args);";
                logger.debug("run before: {}", code);
                ctMethod.insertBefore(code);
                break;
            case AFTER:
                code = className + ".runAfter(\"" + key + "\", $args, $_);";
                logger.debug("run after: {}", code);
                ctMethod.insertAfter(code);
                break;
            default:
                throw new RuntimeException("Unknown position: " + item.position);
        }
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }

    public static void runBefore(String key, Object[] args) {
        try {
            DynamicConfigItem item = DynamicConfigRegistry.getInstance().get(key);
            item.method.invoke(item.instance, new Object[]{convertArgs(args)});
        } catch (Exception e) {
            logger.error("run before failed.", e);
        }
    }

    public static void runAfter(String key, Object[] args, Object returnValue) {
        try {
            DynamicConfigItem item = DynamicConfigRegistry.getInstance().get(key);
            item.method.invoke(item.instance, convertArgs(args), returnValue);
        } catch (Exception e) {
            logger.error("run after failed.", e);
        }
    }

    private static Object[] convertArgs(Object[] args) {
        return args == null ? new Object[0] : args;
    }
}
