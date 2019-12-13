package agent.server.transform.tools.asm;

interface ProxyCallSite {
    ProxyCallConfig getCallConfig();

    Object invokeTargetEntity(Object target, Object[] args) throws Throwable;

    Object invoke(Object target, Object[] args) throws Throwable;

    Class<?>[] getArgTypes();

    Class<?> getReturnType();

    void formatError(Throwable t);
}
