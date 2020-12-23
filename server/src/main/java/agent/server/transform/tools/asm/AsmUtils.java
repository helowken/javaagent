package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.common.utils.DependentClassItem;
import agent.invoke.DestInvoke;
import agent.invoke.data.ClassInvokeItem;
import agent.invoke.data.TypeItem;

import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Map;

public class AsmUtils {
    public static final String ASM_DELEGATE_CLASS = "agent.tools.asm.AsmDelegate";
    private static final DependentClassItem item = DependentClassItem.getInstance();
    private static Method transformMethod;
    private static Method parseTypeMethod;
    private static Method collectMethod;
    private static Method getInvokeFullNameMethod;

    static {
        try {
            transformMethod = ReflectionUtils.getMethod(
                    getDelegateClass(),
                    "transform",
                    Class.class,
                    byte[].class,
                    Map.class
            );
            parseTypeMethod = ReflectionUtils.getMethod(
                    getDelegateClass(),
                    "parseType",
                    String.class
            );
            collectMethod = ReflectionUtils.getMethod(
                    getDelegateClass(),
                    "collect",
                    byte[].class
            );
            getInvokeFullNameMethod = ReflectionUtils.getMethod(
                    getDelegateClass(),
                    "getInvokeFullName",
                    String.class,
                    String.class
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?> getDelegateClass() {
        return item.getDelegateClass(ASM_DELEGATE_CLASS);
    }

    public static byte[] transform(Class<?> sourceClass, byte[] classData, Map<Integer, DestInvoke> idToInvoke) {
        return Utils.wrapToRtError(
                () -> (byte[]) ReflectionUtils.exec(
                        transformMethod,
                        () -> transformMethod.invoke(null, sourceClass, classData, idToInvoke)
                )
        );
    }

    public static void verifyAndPrintResult(ClassLoader loader, byte[] bs, OutputStream out) {
        Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        getDelegateClass(),
                        "verifyAndPrintResult",
                        new Class[]{
                                ClassLoader.class,
                                byte[].class,
                                OutputStream.class
                        },
                        loader,
                        bs,
                        out
                )
        );
    }

    public static void print(byte[] bs, OutputStream out) {
        Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        getDelegateClass(),
                        "print",
                        new Class[]{
                                byte[].class,
                                OutputStream.class
                        },
                        bs,
                        out
                )
        );
    }

    public static TypeItem parseType(String invokeOwner) {
        return Utils.wrapToRtError(
                () -> (TypeItem) parseTypeMethod.invoke(null, invokeOwner)
        );
    }

    public static ClassInvokeItem collect(byte[] classData) {
        return Utils.wrapToRtError(
                () -> (ClassInvokeItem) collectMethod.invoke(null, new Object[]{classData})
        );
    }

    public static String getInvokeFullName(String invokeName, String desc) {
        return Utils.wrapToRtError(
                () -> (String) getInvokeFullNameMethod.invoke(null, new Object[]{invokeName, desc})
        );
    }
}
