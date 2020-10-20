package agent.common.struct;

public interface StructField extends Convertible {
    boolean matchType(Object value);

    default boolean match(Class<?> clazz) {
        return getValueClass() == clazz;
    }

    Class<?> getValueClass();
}
