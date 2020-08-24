package agent.invoke.proxy;


import agent.invoke.DestInvoke;

import java.util.List;

import static agent.invoke.proxy.ProxyArgsMask.useError;
import static agent.invoke.proxy.ProxyPosition.ON_THROWING;

class ProxyCallOnThrowing extends AbstractProxyCall {
    ProxyCallOnThrowing(ProxyCallInfo callInfo) {
        super(ON_THROWING, callInfo);
    }

    @Override
    void collectParams(List<Object> params, int mask, DestInvoke destInvoke, Object pv) {
        if (useError(mask))
            params.add(pv);
    }
}
