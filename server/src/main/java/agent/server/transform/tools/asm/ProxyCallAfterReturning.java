package agent.server.transform.tools.asm;

import static agent.server.transform.tools.asm.ProxyPosition.AFTER_RETURNING;

class ProxyCallAfterReturning extends AbstractProxyCall {
    ProxyCallAfterReturning(ProxyCallInfo callInfo) {
        super(AFTER_RETURNING, callInfo);
    }

    @Override
    public void run(ProxyCallChain callChain) {
        callChain.process();
        if (callChain.isExecuted() && !callChain.hasError())
            exec(callChain);
    }
}
