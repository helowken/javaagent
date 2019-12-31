package agent.server.transform.tools.asm;

import agent.server.transform.impl.invoke.DestInvoke;

import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.useArgTypes;
import static agent.server.transform.tools.asm.ProxyArgsMask.useArgs;
import static agent.server.transform.tools.asm.ProxyPosition.ON_BEFORE;

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
