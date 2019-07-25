package agent.builtin.transformer;

import agent.server.transform.impl.AbstractConfigTransformer;
import javassist.CtClass;
import javassist.CtMethod;

public class TraceMethodInfoTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "sys_traceMethodInfo";


    @Override
    protected void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {

    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
