package agent.server.transform.impl;

import java.lang.reflect.Method;

import static agent.server.transform.impl.ProxyAnnotationConfig.ARGS_NONE;

public abstract class ProxyAnnotationConfigTransformer extends AbstractAnnotationConfigTransformer {

    @Override
    protected Object[] getOtherArgs(Method srcMethod, Method anntMethod, int argsHint) {
        Object[] otherArgs = null;
        if (argsHint != ARGS_NONE)
            otherArgs = newOtherArgs(srcMethod, anntMethod, argsHint);
        if (otherArgs == null)
            otherArgs = new Object[0];
        return new Object[]{
                otherArgs
        };
    }

    protected abstract Object[] newOtherArgs(Method srcMethod, Method anntMethod, int argsHint);
}
