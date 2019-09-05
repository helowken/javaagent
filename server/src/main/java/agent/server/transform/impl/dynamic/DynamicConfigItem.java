package agent.server.transform.impl.dynamic;

import agent.server.transform.config.rule.MethodRule.Position;

import java.lang.reflect.Method;

public class DynamicConfigItem {
    public final Position position;
    public final Method ruleMethod;
    public final Object ruleInstance;
    public final MethodCallFilter methodCallFilter;
    public final boolean needMethodInfo;
    public final boolean needMethodCallInfo;
    public final boolean needPosition;
    public final boolean needReturnValue;

    public DynamicConfigItem(Position position, Method ruleMethod, Object ruleInstance, MethodCallFilter methodCallFilter) {
        this.position = position;
        this.ruleMethod = ruleMethod;
        this.ruleInstance = ruleInstance;
        this.methodCallFilter = methodCallFilter;
        this.needMethodInfo = needMethodInfo(ruleMethod);
        this.needMethodCallInfo = needMethodCallInfo(ruleMethod);
        this.needPosition = needPosition(position);
        this.needReturnValue = needReturnValue(position);
    }

    private boolean needPosition(Position position) {
        switch (position) {
            case BEFORE:
            case AFTER:
            case BEFORE_MC:
            case AFTER_MC:
                return false;
            case WRAP:
            case WRAP_MC:
                return true;
        }
        throw newError(position);
    }

    private boolean needReturnValue(Position position) {
        switch (position) {
            case BEFORE:
            case BEFORE_MC:
                return false;
            case AFTER:
            case AFTER_MC:
            case WRAP:
            case WRAP_MC:
                return true;
        }
        throw newError(position);
    }

    private RuntimeException newError(Position position) {
        return new RuntimeException("Invalid position: " + position);
    }

    private boolean needMethodInfo(Method method) {
        return containsParamType(method, MethodInfo.class);
    }

    private boolean needMethodCallInfo(Method method) {
        return containsParamType(method, MethodCallInfo.class);
    }

    private boolean containsParamType(Method method, Class<?> paramType) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes != null) {
            for (Class<?> pt : paramTypes) {
                if (pt.equals(paramType))
                    return true;
            }
        }
        return false;
    }
}
