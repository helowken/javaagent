package agent.server.transform.tools.asm;

import java.lang.reflect.Constructor;

class ConstructorProxyCallSite extends AbstractProxyCallSite {

    ConstructorProxyCallSite(ProxyCallSiteConfig config) {
        super(config);
    }

    @Override
    public Object invokeTargetEntity(Object target, Object[] args) throws Throwable {
        return null;
    }

    private Constructor getConstructor() {
        return (Constructor) getCallConfig().getDestInvoke().getSourceEntity();
    }

    @Override
    public Class<?>[] getArgTypes() {
        return getConstructor().getParameterTypes();
    }

    @Override
    public Class<?> getReturnType() {
        return void.class;
    }

    @Override
    public void formatError(Throwable t) {
    }
}
