package agent.server.transform.tools.asm;

import static agent.server.transform.tools.asm.ProxyPosition.*;

class ProxyCallAfter extends AbstractProxyCall {
    ProxyCallAfter(ProxyCallInfo callInfo) {
        super(AFTER, callInfo);
    }

    @Override
    public void run(ProxyCallChain callChain) {
        callChain.process();
        exec(callChain);
    }
}


