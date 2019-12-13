package agent.server.transform.tools.asm;

public class ProxyArgsMask {
    public static final int MASK_NONE = 0;
    public static final int MASK_ARGS = 1;
    public static final int MASK_ARG_TYPES = 2;
    public static final int MASK_RETURN_VALUE = 4;
    public static final int MASK_RETURN_TYPE = 8;
    public static final int MASK_ERROR = 16;
    public static final int MASK_INVOKE_TARGET = 32;
    public static final int MASK_INVOKE_METHOD = 64;
    public static final int MASK_PROXY_CHAIN = 128;

    public static final int DEFAULT_BEFORE = MASK_ARGS | MASK_ARG_TYPES;
    public static final int DEFAULT_AFTER_RETURNING = MASK_RETURN_VALUE | MASK_RETURN_TYPE;
    public static final int DEFAULT_AFTER_THROWING = MASK_ERROR;
    public static final int DEFAULT_AFTER = DEFAULT_AFTER_RETURNING | DEFAULT_AFTER_THROWING;
    public static final int DEFAULT_AROUND = MASK_PROXY_CHAIN;

    static boolean useArgs(int mask) {
        return (mask & MASK_ARGS) != 0;
    }

    static boolean useArgTypes(int mask) {
        return (mask & MASK_ARG_TYPES) != 0;
    }

    static boolean useReturnValue(int mask) {
        return (mask & MASK_RETURN_VALUE) != 0;
    }

    static boolean useReturnType(int mask) {
        return (mask & MASK_RETURN_TYPE) != 0;
    }

    static boolean useError(int mask) {
        return (mask & MASK_ERROR) != 0;
    }

    static boolean useInvokeTarget(int mask) {
        return (mask & MASK_INVOKE_TARGET) != 0;
    }

    static boolean useInvokeMethod(int mask) {
        return (mask & MASK_INVOKE_METHOD) != 0;
    }

    static boolean useProxyChain(int mask) {
        return (mask & MASK_PROXY_CHAIN) != 0;
    }
}
