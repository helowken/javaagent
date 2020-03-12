package agent.server.transform.impl.invoke;

public interface DestInvoke {
    int getModifiers();

    DestInvokeType getType();

    String getName();

    String getDescriptor();

    Class<?> getDeclaringClass();

    Object getInvokeEntity();

    Class<?>[] getParamTypes();

    Class<?> getReturnType();

    boolean isStatic();

    default String getFullName() {
        return getName() + getDescriptor();
    }
}
