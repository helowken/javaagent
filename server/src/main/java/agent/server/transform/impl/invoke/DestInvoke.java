package agent.server.transform.impl.invoke;

public interface DestInvoke {
    DestInvokeType getType();

    String getName();

    String getDescriptor();

    Class<?> getDeclaringClass();

    Object getInvokeEntity();

    Class<?>[] getParamTypes();

    Class<?> getReturnType();
}
