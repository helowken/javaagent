package agent.builtin.transformer.utils;

import agent.base.utils.Logger;
import agent.base.utils.ReflectionUtils;

public class MethodLogger {
    private static final Logger logger = Logger.getLogger(MethodLogger.class);
    private final StringBuilder sb = new StringBuilder();
    private final String printerClass;
    private volatile MethodPrinter printer;

    public MethodLogger(String printerClass) {
        this.printerClass = printerClass;
    }

    public void printArgs(Object[] args, Class<?>[] argClasses) {
        doPrint(printer -> printer.printArgs(sb, args, argClasses));
    }

    public void printReturnValue(Object returnValue, Class<?> returnValueClass) {
        doPrint(printer -> printer.printReturnValue(sb, returnValue, returnValueClass));
    }

    public String getContent() {
        return sb.toString();
    }

    private void doPrint(PrintFunc func) {
        try {
            func.exec(getPrinter());
        } catch (Exception e) {
            logger.error("Print method failed.", e);
        }
    }

    private MethodPrinter getPrinter() throws Exception {
        if (printer == null) {
            synchronized (this) {
                if (printer == null)
                    printer = ReflectionUtils.newInstance(printerClass);
            }
        }
        return printer;
    }

    private interface PrintFunc {
        void exec(MethodPrinter methodPrinter);
    }
}
