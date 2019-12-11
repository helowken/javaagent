package agent.server.transform.tools.asm;

import org.objectweb.asm.Type;

import java.util.HashMap;
import java.util.Map;

import static agent.server.transform.tools.asm.AsmMethod.newInvokeStatic;
import static agent.server.transform.tools.asm.AsmMethod.newInvokeVirtual;

class PrimitiveWrapper {
    private static final Map<String, WrapperItem> primitiveDescToWrapperItem = new HashMap<>();

    static {
        populate(Type.BOOLEAN_TYPE, Boolean.class);
        populate(Type.CHAR_TYPE, Character.class);
        populate(Type.BYTE_TYPE, Byte.class);
        populate(Type.SHORT_TYPE, Short.class);
        populate(Type.INT_TYPE, Integer.class);
        populate(Type.FLOAT_TYPE, Float.class);
        populate(Type.LONG_TYPE, Long.class);
        populate(Type.DOUBLE_TYPE, Double.class);
    }

    private static void populate(Type primitiveType, Class<?> wrapperClass) {
        Type wrapperType = Type.getType(wrapperClass);
        String primitiveDesc = primitiveType.getDescriptor();
        primitiveDescToWrapperItem.put(
                primitiveDesc,
                new WrapperItem(
                        wrapperType,
                        wrapperType.getInternalName(),
                        "valueOf",
                        "(" + primitiveDesc + ")" + wrapperType.getDescriptor(),
                        primitiveType.getClassName() + "Value",
                        "()" + primitiveDesc
                )
        );
    }

    static Type getWrapperType(String desc) {
        WrapperItem item = primitiveDescToWrapperItem.get(desc);
        return item == null ? null : item.wrapperType;
    }

    static Object mayCreateWrapCallNode(String desc) {
        WrapperItem item = primitiveDescToWrapperItem.get(desc);
        return item == null ?
                null :
                newInvokeStatic(item.owner, item.wrapMethod, item.wrapMethodDesc);
    }

    static Object mayCreateUnwrapCallNode(String desc) {
        WrapperItem item = primitiveDescToWrapperItem.get(desc);
        return item == null ?
                null :
                newInvokeVirtual(item.owner, item.unwrapMethod, item.unwrapMethodDesc);
    }

    private static class WrapperItem {
        final Type wrapperType;
        final String owner;
        final String wrapMethod;
        final String wrapMethodDesc;
        final String unwrapMethod;
        final String unwrapMethodDesc;

        private WrapperItem(Type wrapperType, String owner, String wrapMethod, String wrapMethodDesc,
                            String unwrapMethod, String unwrapMethodDesc) {
            this.wrapperType = wrapperType;
            this.owner = owner;
            this.wrapMethod = wrapMethod;
            this.wrapMethodDesc = wrapMethodDesc;
            this.unwrapMethod = unwrapMethod;
            this.unwrapMethodDesc = unwrapMethodDesc;
        }
    }
}
