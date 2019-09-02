package agent.server.transform.impl;

import agent.base.utils.Logger;
import javassist.CtClass;
import javassist.CtMethod;

public class DynamicClassTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "$sys_dynamic";
    public static final String KEY_POSITION = "position";
    public static final String KEY_METHOD = "method";
    public static final String KEY_INSTANCE = "instance";

    private static final Logger logger = Logger.getLogger(DynamicClassTransformer.class);

    @Override
    protected void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {

    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
