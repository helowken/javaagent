package agent.builtin.transformer.utils;

import agent.base.utils.IndentUtils;

public class DefaultMethodPrinter implements MethodPrinter {

    @Override
    public void printArgs(StringBuilder sb, Object[] args, Class<?>[] argClasses) {
        if (args.length != argClasses.length)
            throw new RuntimeException("Length of args != length of argClasses");
        for (int i = 0; i < args.length; ++i) {
            sb.append(IndentUtils.getIndent(1))
                    .append("Arg ").append(i).append(" ")
                    .append(getClassName(argClasses[i]))
                    .append(": ");
            printObject(sb, args[i], argClasses[i]);
            sb.append("\n");
        }
    }

    @Override
    public void printReturnValue(StringBuilder sb, Object returnValue, Class<?> returnValueClass) {
        sb.append(IndentUtils.getIndent(1))
                .append("Return Value ")
                .append(getClassName(returnValueClass))
                .append(": ");
        printObject(sb, returnValue, returnValueClass);
        sb.append("\n");
    }

    private String getClassName(Class<?> clazz) {
        return "[" + clazz.getName() + "]";
    }

    protected void printObject(StringBuilder sb, Object obj, Class<?> objClass) {
        if (obj == null)
            sb.append("null");
        else {
            sb.append(obj);
        }
    }
}
