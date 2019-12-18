package agent.server.transform.tools.asm;

interface DestInvoke {
    DestInvokeType getType();

    String getName();

    String getDescriptor();

    Class<?> getDeclaringClass();

    Object getInvokeEntity();

    Class<?>[] getParamTypes();

    Class<?> getReturnType();
}
