package agent.server.transform.tools.asm;

import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.useArgTypes;
import static agent.server.transform.tools.asm.ProxyArgsMask.useArgs;
import static agent.server.transform.tools.asm.ProxyPosition.BEFORE;

class ProxyCallBefore extends AbstractProxyCall {
    ProxyCallBefore(ProxyCallInfo callInfo) {
        super(BEFORE, callInfo);
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
