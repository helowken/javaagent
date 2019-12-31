package agent.server.transform.tools.asm;

import agent.server.transform.impl.invoke.DestInvoke;

import java.util.List;

import static agent.server.transform.tools.asm.ProxyPosition.ON_AFTER;

class ProxyCallAfter extends AbstractProxyCall {
    ProxyCallAfter(ProxyCallInfo callInfo) {
        super(ON_AFTER, callInfo);
    }

    @Override
    void collectParams(List<Object> params, int mask, DestInvoke destInvoke, Object pv) {
    }
}
