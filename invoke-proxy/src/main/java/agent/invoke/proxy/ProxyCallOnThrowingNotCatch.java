package agent.invoke.proxy;


import agent.invoke.DestInvoke;

import java.util.List;

import static agent.invoke.proxy.ProxyArgsMask.useError;
import static agent.invoke.proxy.ProxyPosition.ON_THROWING_NOT_CATCH;

class ProxyCallOnThrowingNotCatch extends AbstractProxyCall {
    ProxyCallOnThrowingNotCatch(ProxyCallInfo callInfo) {
        super(ON_THROWING_NOT_CATCH, callInfo);
    }

    @Override
    void collectParams(List<Object> params, int mask, DestInvoke destInvoke, Object pv) {
        if (useError(mask))
            params.add(pv);
    }
}
