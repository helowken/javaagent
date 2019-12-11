package agent.server.transform.tools.asm;

import static agent.server.transform.tools.asm.ProxyPosition.AFTER_THROWING;

class ProxyCallAfterThrowing extends AbstractProxyCall {
    ProxyCallAfterThrowing(ProxyCallInfo callInfo) {
        super(AFTER_THROWING, callInfo);
    }

    @Override
    public void run(ProxyCallChain callChain) {
        callChain.process();
        if (callChain.isExecuted() && callChain.hasError())
            exec(callChain);
    }
}
