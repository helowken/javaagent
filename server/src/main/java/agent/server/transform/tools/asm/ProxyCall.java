package agent.server.transform.tools.asm;

import agent.server.transform.impl.invoke.DestInvoke;

interface ProxyCall {
    void run(DestInvoke destInvoke, Object instanceOrNull, Object pv);

    ProxyCallInfo getCallInfo();
}
