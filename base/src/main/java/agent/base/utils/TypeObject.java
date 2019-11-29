package agent.base.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeObject<T> {
    public final Type type;

    protected TypeObject() {
        Type superClass = this.getClass().getGenericSuperclass();
        if (superClass instanceof Class) {
            throw new IllegalArgumentException(" without actual type information");
        } else {
            this.type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        }
    }
}
