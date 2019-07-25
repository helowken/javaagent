package agent.server.transform.impl.user;

import agent.server.transform.impl.AbstractConfigTransformer;
import javassist.CtClass;
import javassist.CtMethod;

public class TraceMethodInfoTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "traceMethodInfo";


    @Override
    protected void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception {

    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
