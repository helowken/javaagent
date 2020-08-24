package agent.invoke.proxy;


import agent.invoke.DestInvoke;

import java.util.List;

import static agent.invoke.proxy.ProxyPosition.ON_AFTER;


class ProxyCallAfter extends AbstractProxyCall {
    ProxyCallAfter(ProxyCallInfo callInfo) {
        super(ON_AFTER, callInfo);
    }

    @Override
    void collectParams(List<Object> params, int mask, DestInvoke destInvoke, Object pv) {
    }
}
