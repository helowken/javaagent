package agent.server.transform.impl.dynamic;

public class RuleValidateMgr {

    private static boolean isValidType(Class<?>[] paramTypes, int idx, Class<?> expectedType) {
        Class<?> paramType = idx < paramTypes.length ?
                paramTypes[idx] :
                null;
        return expectedType.equals(paramType);
    }

    private static void doCheck(boolean valid, DynamicConfigItem item) {
        if (!valid)
            throw new RuntimeException("Invalid method signature, expected is: " + getMethodLongName(item));
    }

    private static String getMethodLongName(DynamicConfigItem item) {
        String s = "void method(Object[] args, Object returnValue [, " +
                MethodInfo.class.getSimpleName() + " methodInfo] [, " +
                MethodCallInfo.class.getSimpleName() + " methodCallInfo]";
        if (item.needPosition)
            s += ", boolean isBefore";
        s += ")";
        return s;
    }

    public static void checkMethodValid(DynamicConfigItem item) {
        Class<?>[] paramTypes = item.ruleMethod.getParameterTypes();
        if (paramTypes == null)
            doCheck(false, item);

        int idx = 0;
        doCheck(isValidType(paramTypes, idx++, Object[].class), item);

        if (item.needReturnValue)
            doCheck(isValidType(paramTypes, idx++, Object.class), item);

        if (item.needMethodInfo)
            doCheck(isValidType(paramTypes, idx++, MethodInfo.class), item);

        if (item.needMethodCallInfo)
            doCheck(isValidType(paramTypes, idx++, MethodCallInfo.class), item);

        if (item.needPosition)
            doCheck(isValidType(paramTypes, idx, boolean.class), item);
    }

}
