package agent.server.transform.tools.asm;

import agent.server.transform.impl.invoke.DestInvoke;

import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.useError;
import static agent.server.transform.tools.asm.ProxyPosition.ON_THROWING;

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
