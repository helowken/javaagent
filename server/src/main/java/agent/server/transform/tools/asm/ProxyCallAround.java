package agent.server.transform.tools.asm;

import static agent.server.transform.tools.asm.ProxyPosition.AROUND;

class ProxyCallAround extends AbstractProxyCall {
    ProxyCallAround(ProxyCallInfo callInfo) {
        super(AROUND, callInfo);
    }

    @Override
    public void run(ProxyCallChain callChain) {
        exec(callChain);
    }
}
