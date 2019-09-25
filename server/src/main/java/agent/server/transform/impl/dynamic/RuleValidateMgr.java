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
            throw new RuntimeException("Invalid method signature: " + item.ruleMethod + ", expected is: " + getMethodLongName(item));
    }

    private static String getMethodLongName(DynamicConfigItem item) {
        String s = "void method(Object[] args, Object returnValue [, " +
                MethodInfo.class.getSimpleName() + " methodInfo]";
        if (item.needPosition)
            s += ", boolean isBefore";
        s += ")";
        return s;
    }

    public static void checkMethodValid(DynamicConfigItem item) {
        Class<?>[] paramTypes = item.ruleMethod.getParameterTypes();
        doCheck(paramTypes != null, item);

        int idx = 0;
        doCheck(isValidType(paramTypes, idx++, Object[].class), item);

        if (item.needReturnValue)
            doCheck(isValidType(paramTypes, idx++, Object.class), item);

        if (item.needMethodInfo)
            doCheck(isValidType(paramTypes, idx++, MethodInfo.class), item);

        if (item.needPosition)
            doCheck(isValidType(paramTypes, idx, boolean.class), item);
    }

}
