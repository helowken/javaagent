package agent.server.transform.tools.asm;

import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.useReturnType;
import static agent.server.transform.tools.asm.ProxyArgsMask.useReturnValue;
import static agent.server.transform.tools.asm.ProxyPosition.ON_RETURNING;

class ProxyCallOnReturning extends AbstractProxyCall {
    ProxyCallOnReturning(ProxyCallInfo callInfo) {
        super(ON_RETURNING, callInfo);
    }

    @Override
    void collectParams(List<Object> params, int mask, DestInvoke destInvoke, Object pv) {
        if (useReturnValue(mask))
            params.add(pv);
        if (useReturnType(mask))
            params.add(
                    destInvoke.getReturnType()
            );
    }
}
