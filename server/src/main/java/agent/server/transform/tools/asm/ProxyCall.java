package agent.server.transform.tools.asm;

interface ProxyCall {
    void run(DestInvoke destInvoke, Object instanceOrNull, Object pv);
}
