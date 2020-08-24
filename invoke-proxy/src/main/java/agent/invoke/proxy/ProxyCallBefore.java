package agent.invoke.proxy;


import agent.invoke.DestInvoke;

import java.util.List;

import static agent.invoke.proxy.ProxyArgsMask.useArgTypes;
import static agent.invoke.proxy.ProxyArgsMask.useArgs;
import static agent.invoke.proxy.ProxyPosition.ON_BEFORE;


class ProxyCallBefore extends AbstractProxyCall {
    ProxyCallBefore(ProxyCallInfo callInfo) {
        super(ON_BEFORE, callInfo);
    }

    @Override
    void collectParams(List<Object> params, int mask, DestInvoke destInvoke, Object pv) {
        if (useArgs(mask))
            params.add(pv);
        if (useArgTypes(mask))
            params.add(
                    destInvoke.getParamTypes()
            );
    }
}
