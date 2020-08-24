package agent.invoke;


import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.util.Objects;

public class ConstructorInvoke implements DestInvoke {
    private final Constructor constructor;

    public ConstructorInvoke(Constructor<?> constructor) {
        if (constructor == null)
            throw new IllegalArgumentException("Constructor is null!");
        this.constructor = constructor;
    }

    @Override
    public int getModifiers() {
        return constructor.getModifiers();
    }

    @Override
    public DestInvokeType getType() {
        return DestInvokeType.CONSTRUCTOR;
    }

    @Override
    public String getName() {
        return ReflectionUtils.CONSTRUCTOR_NAME;
    }

    @Override
    public String getDescriptor() {
        return InvokeDescriptorUtils.getDescriptor(constructor);
    }

    @Override
    public Class<?> getDeclaringClass() {
        return constructor.getDeclaringClass();
    }

    @Override
    public Object getInvokeEntity() {
        return constructor;
    }

    @Override
    public Class<?>[] getParamTypes() {
        return constructor.getParameterTypes();
    }

    @Override
    public Class<?> getReturnType() {
        return void.class;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstructorInvoke that = (ConstructorInvoke) o;
        return Objects.equals(constructor, that.constructor);
    }

    @Override
    public int hashCode() {

        return Objects.hash(constructor);
    }

    @Override
    public String toString() {
        return constructor.toString();
    }
}
