package agent.server.transform.impl.invoke;

import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.Objects;

public class MethodInvoke implements DestInvoke {
    private final Method method;

    public MethodInvoke(Method method) {
        if (method == null)
            throw new IllegalArgumentException("Method is null!");
        this.method = method;
    }

    @Override
    public int getModifiers() {
        return method.getModifiers();
    }

    @Override
    public DestInvokeType getType() {
        return DestInvokeType.METHOD;
    }

    @Override
    public String getName() {
        return method.getName();
    }

    @Override
    public String getDescriptor() {
        return Type.getMethodDescriptor(method);
    }

    @Override
    public Class<?> getDeclaringClass() {
        return method.getDeclaringClass();
    }

    @Override
    public Object getInvokeEntity() {
        return method;
    }

    @Override
    public Class<?>[] getParamTypes() {
        return method.getParameterTypes();
    }

    @Override
    public Class<?> getReturnType() {
        return method.getReturnType();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodInvoke that = (MethodInvoke) o;
        return Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {

        return Objects.hash(method);
    }

    @Override
    public String toString() {
        return method.toString();
    }
}
