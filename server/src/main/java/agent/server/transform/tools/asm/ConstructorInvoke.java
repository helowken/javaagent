package agent.server.transform.tools.asm;


import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.util.Objects;

class ConstructorInvoke implements DestInvoke {
    private final Constructor constructor;

    ConstructorInvoke(Constructor<?> constructor) {
        if (constructor == null)
            throw new IllegalArgumentException("Constructor is null!");
        this.constructor = constructor;
    }

    @Override
    public DestInvokeType getType() {
        return DestInvokeType.CONSTRUCTOR;
    }

    @Override
    public String getName() {
        return "<init>";
    }

    @Override
    public String getDescriptor() {
        return Type.getConstructorDescriptor(constructor);
    }

    @Override
    public Class<?> getDeclaringClass() {
        return constructor.getDeclaringClass();
    }

    @Override
    public Object getSourceEntity() {
        return constructor;
    }

    @Override
    public ProxyCallSite newCallSite(ProxyCallSiteConfig config) {
        return new ConstructorProxyCallSite(config);
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
        return "ConstructorInvoke{" +
                "constructor=" + constructor +
                '}';
    }
}
