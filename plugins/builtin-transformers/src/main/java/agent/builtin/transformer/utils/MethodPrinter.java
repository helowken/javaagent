package agent.builtin.transformer.utils;

public interface MethodPrinter {
    void printArgs(StringBuilder sb, Object[] args);

    void printReturnValue(StringBuilder sb, Object returnValue);
}
