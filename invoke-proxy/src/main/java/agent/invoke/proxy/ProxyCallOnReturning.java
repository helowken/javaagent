package agent.invoke.proxy;


import agent.invoke.DestInvoke;

import java.util.List;

import static agent.invoke.proxy.ProxyArgsMask.useReturnType;
import static agent.invoke.proxy.ProxyArgsMask.useReturnValue;
import static agent.invoke.proxy.ProxyPosition.ON_RETURNING;

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
