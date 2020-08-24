package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.common.utils.DelegateClassItem;
import agent.invoke.DestInvoke;
import agent.invoke.data.ClassInvokeItem;
import agent.invoke.data.TypeItem;

import java.io.OutputStream;
import java.util.Map;

public class AsmUtils {
    public static final String ASM_DELEGATE_CLASS = "agent.tools.asm.AsmDelegate";
    private static final DelegateClassItem item = DelegateClassItem.getInstance();

    public static byte[] transform(Class<?> sourceClass, byte[] classData, Map<Integer, DestInvoke> idToInvoke) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        item.getDelegateClass(ASM_DELEGATE_CLASS),
                        "transform",
                        new Class[]{
                                Class.class,
                                byte[].class,
                                Map.class
                        },
                        sourceClass,
                        classData,
                        idToInvoke
                )
        );
    }

    public static void verifyAndPrintResult(ClassLoader loader, byte[] bs, OutputStream out) {
        Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        item.getDelegateClass(ASM_DELEGATE_CLASS),
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
                        item.getDelegateClass(ASM_DELEGATE_CLASS),
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
                () -> ReflectionUtils.invokeStatic(
                        item.getDelegateClass(ASM_DELEGATE_CLASS),
                        "parseType",
                        new Class[]{String.class},
                        invokeOwner
                )
        );
    }

    public static ClassInvokeItem collect(byte[] classData) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        item.getDelegateClass(ASM_DELEGATE_CLASS),
                        "collect",
                        new Class[]{byte[].class},
                        new Object[]{classData}
                )
        );
    }
}
