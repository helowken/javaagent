package agent.server.transform.tools.asm;

import java.util.List;

import static agent.server.transform.tools.asm.ProxyPosition.AFTER;

class ProxyCallAfter extends AbstractProxyCall {
    ProxyCallAfter(ProxyCallInfo callInfo) {
        super(AFTER, callInfo);
    }

    @Override
    void collectParams(List<Object> params, int mask, DestInvoke destInvoke, Object pv) {
    }
}
