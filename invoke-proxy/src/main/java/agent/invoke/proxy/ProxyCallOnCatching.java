package agent.invoke.proxy;

import agent.invoke.DestInvoke;

import java.util.List;

import static agent.invoke.proxy.ProxyArgsMask.useError;
import static agent.invoke.proxy.ProxyPosition.ON_CATCHING;

class ProxyCallOnCatching extends AbstractProxyCall {
    ProxyCallOnCatching(ProxyCallInfo callInfo) {
        super(ON_CATCHING, callInfo);
    }

    @Override
    void collectParams(List<Object> params, int mask, DestInvoke destInvoke, Object pv) {
        if (useError(mask))
            params.add(pv);
    }
}
