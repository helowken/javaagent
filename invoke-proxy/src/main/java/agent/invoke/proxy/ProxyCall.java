package agent.invoke.proxy;


import agent.invoke.DestInvoke;

interface ProxyCall {
    void run(DestInvoke destInvoke, Object instanceOrNull, Object pv);

    ProxyCallInfo getCallInfo();
}
