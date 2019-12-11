package agent.server.transform.tools.asm;

import static agent.server.transform.tools.asm.ProxyPosition.BEFORE;

class ProxyCallBefore extends AbstractProxyCall {
    ProxyCallBefore(ProxyCallInfo callInfo) {
        super(BEFORE, callInfo);
    }

    @Override
    public void run(ProxyCallChain callChain) {
        exec(callChain);
        callChain.process();
    }
}
