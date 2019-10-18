package agent.builtin.transformer.utils;

public interface MethodPrinter {
    void printArgs(StringBuilder sb, Object[] args, Class<?>[] argClasses);

    void printReturnValue(StringBuilder sb, Object returnValue, Class<?> returnValueClass);
}
