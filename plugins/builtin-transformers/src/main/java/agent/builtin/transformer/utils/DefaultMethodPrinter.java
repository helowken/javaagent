package agent.builtin.transformer.utils;

import agent.base.utils.IndentUtils;
import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;

import java.lang.reflect.Modifier;

public class DefaultMethodPrinter implements MethodPrinter {
    private static final Logger logger = Logger.getLogger(DefaultMethodPrinter.class);

    @Override
    public void printArgs(StringBuilder sb, Object[] args) {
        for (int i = 0; i < args.length; ++i) {
            sb.append(IndentUtils.getIndent(1))
                    .append("Arg ").append(i).append(" ")
                    .append(getClassName(args[i])).append(": ");
            printObject(sb, args[i]);
            sb.append("\n");
        }
    }

    @Override
    public void printReturnValue(StringBuilder sb, Object returnValue) {
        sb.append(IndentUtils.getIndent(1))
                .append("Return Value ")
                .append(getClassName(returnValue)).append(": ");
        printObject(sb, returnValue);
        sb.append("\n");
    }

    private String getClassName(Object v) {
        return "[" + (
                v == null ? "" : v.getClass().getName()
        ) + "]";
    }

    protected void printObject(StringBuilder sb, Object obj) {
        if (obj == null)
            sb.append("null");
        else {
            sb.append(obj);
//            Class<?> tmpClass = obj.getClass();
//            try {
//                while (tmpClass != null) {
//                    ReflectionUtils.useDeclaredFields(tmpClass,
//                            field -> {
//                                if (Modifier.isStatic(field.getModifiers())) {
//
//                                } else {
//
//                                }
//                            }
//                    );
//                    tmpClass = tmpClass.getSuperclass();
//                }
//            } catch (Exception e) {
//                sb.append("#Get value error!#");
//                logger.error("Get value failed.", e);
//            }
        }
    }
}
