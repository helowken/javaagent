package agent.server.transform.tools.asm;

abstract class AbstractProxyCallSite implements ProxyCallSite {
    final ProxyCallSiteConfig config;

    AbstractProxyCallSite(ProxyCallSiteConfig config) {
        this.config = config;
    }

    public ProxyCallConfig getCallConfig() {
        return config.callConfig;
    }

    public Object invoke(Object target, Object[] args) throws Throwable {
        ProxyCallChain chain = new ProxyCallChain(this, target, args);
        chain.process();
        if (chain.hasError())
            throw chain.getError();
        return chain.getReturnValue();
    }

}
