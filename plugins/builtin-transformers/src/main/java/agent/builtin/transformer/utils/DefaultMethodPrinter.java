package agent.builtin.transformer.utils;

public class DefaultMethodPrinter implements MethodPrinter {
    @Override
    public void printArgs(StringBuilder sb, Object[] args) {
        for (int i = 0; i < args.length; ++i) {
            sb.append("Arg ").append(i).append(": ").append(args[i]).append("\n");
        }
    }

    @Override
    public void printReturnValue(StringBuilder sb, Object returnValue) {
        sb.append("Return Value: ").append(returnValue);
    }
}
