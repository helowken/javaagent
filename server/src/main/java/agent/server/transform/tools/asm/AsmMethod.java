package agent.server.transform.tools.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.objectweb.asm.Opcodes.*;

class AsmMethod {
    private final MethodNode methodNode;
    private final InsnList insnList;

    private AsmMethod(MethodNode methodNode) {
        this.methodNode = methodNode;
        insnList = methodNode.instructions;
    }

    Type getReturnType() {
        return Type.getReturnType(
                methodNode.desc
        );
    }

    boolean isVoid() {
        return getReturnType().getSort() == Type.VOID;
    }

    MethodNode getMethodNode() {
        return methodNode;
    }

    AsmMethod add(Object... ns) {
        if (ns != null) {
            for (Object n : ns) {
                if (n instanceof InsnList)
                    insnList.add((InsnList) n);
                else if (n instanceof AbstractInsnNode)
                    insnList.add((AbstractInsnNode) n);
                else if (n instanceof Object[])
                    add((Object[]) n);
                else if (n instanceof Collection)
                    add(((Collection) n).toArray());
                else
                    throw new IllegalArgumentException("Invalid argument: " + n);
            }
        }
        return this;
    }

    static AsmMethod copyFrom(MethodNode methodNode) {
        return new AsmMethod(
                new MethodNode(
                        methodNode.access,
                        methodNode.name,
                        methodNode.desc,
                        methodNode.signature,
                        methodNode.exceptions.toArray(new String[0])
                )
        );
    }

    static List<LocalVariableNode> getArguments(MethodNode methodNode) {
        List<LocalVariableNode> localVariables = new ArrayList<>(methodNode.localVariables);
        if (!isStatic(methodNode))
            localVariables.remove(0);
        return localVariables;
    }

    static Object newLoad(LocalVariableNode localVariable) {
        return newLoad(localVariable.desc, localVariable.index);
    }

    static Object newLoad(String desc, int index) {
        return newLoad(
                Type.getType(desc),
                index
        );
    }

    static Object newLoad(Class<?> clazz, int index) {
        return newLoad(
                Type.getType(clazz),
                index
        );
    }

    static Object newLoad(Type type, int index) {
        return new VarInsnNode(
                type.getOpcode(ILOAD),
                index
        );
    }

    static Object newLoadWrapPrimitive(LocalVariableNode localVariable) {
        Object load = AsmMethod.newLoad(localVariable);
        Object wrapCall = PrimitiveWrapper.mayCreateWrapCallNode(
                localVariable.desc
        );
        return wrapCall != null ?
                new Object[]{load, wrapCall} :
                load;
    }

    static Object newLoads(List<LocalVariableNode> localVariables) {
        return localVariables
                .stream()
                .map(AsmMethod::newLoad)
                .toArray();
    }

    static Object newLoadClassName(LocalVariableNode localVariable) {
        return newLoadClassName(localVariable.desc);
    }

    static Object newLoadClassName(String desc) {
        return newLoadLdc(
                Type.getType(desc).getClassName()
        );
    }

    static Object newLoadClass(String internalName) {
        return newLoadLdc(
                Type.getType(internalName)
        );
    }

    static Object newLoadLdc(Object value) {
        return new LdcInsnNode(value);
    }

    static boolean isStatic(MethodNode methodNode) {
        return (methodNode.access & ACC_STATIC) != 0;
    }


    static Object newThrow() {
        return new InsnNode(ATHROW);
    }

    static Object newReturn(String desc) {
        return newReturn(
                Type.getType(desc).getReturnType()
        );
    }

    static Object newReturn(Type returnType) {
        return new InsnNode(
                returnType.getOpcode(IRETURN)
        );
    }

    static TryCatchBlockNode newTryCatch() {
        return new TryCatchBlockNode(
                new LabelNode(),
                new LabelNode(),
                new LabelNode(),
                null
        );
    }

    static Object newArray(Class<?> clazz, int len) {
        return new Object[]{
                newNumLoad(len),
                new TypeInsnNode(
                        ANEWARRAY,
                        Type.getInternalName(clazz)
                )
        };
    }

    static <T> Object populateArray(Class<?> clazz, List<T> nodes, ValueConverter<T> arrayValueSupplier) {
        List<Object> rsList = new ArrayList<>();
        int index = 0;
        for (T node : nodes) {
            rsList.add(
                    setValueToArray(
                            clazz,
                            index,
                            arrayValueSupplier.convert(node)
                    )
            );
            ++index;
        }
        return rsList;
    }

    static Object setValueToArray(Class<?> clazz, int index, Object value) {
        return new Object[]{
                new InsnNode(DUP),
                newNumLoad(index),
                value,
                new InsnNode(
                        Type.getType(clazz).getOpcode(IASTORE)
                )
        };
    }

    static Object newLoadNull(int count) {
        List<Object> rsList = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            rsList.add(
                    new InsnNode(ACONST_NULL)
            );
        }
        return rsList;
    }

    static Object newStore(Class<?> clazz, int index) {
        return newStore(
                Type.getType(clazz),
                index
        );
    }

    static Object newStore(Type type, int index) {
        return new VarInsnNode(
                type.getOpcode(ISTORE),
                index
        );
    }

    static Object newObject(Constructor constructor, AsmFunc addArgsFunc) {
        Type type = Type.getType(
                constructor.getDeclaringClass()
        );
        return new Object[]{
                new TypeInsnNode(
                        NEW,
                        type.getInternalName()
                ),
                new InsnNode(DUP),
                addArgsFunc.run(),
                new MethodInsnNode(
                        INVOKESPECIAL,
                        type.getInternalName(),
                        "<init>",
                        Type.getType(constructor).getDescriptor(),
                        false
                )
        };
    }

    static Object newNumLoad(int n) {
        int opcode = NOP;
        switch (n) {
            case -1:
                opcode = ICONST_M1;
                break;
            case 0:
                opcode = ICONST_0;
                break;
            case 1:
                opcode = ICONST_1;
                break;
            case 2:
                opcode = ICONST_2;
                break;
            case 3:
                opcode = ICONST_3;
                break;
            case 4:
                opcode = ICONST_4;
                break;
            case 5:
                opcode = ICONST_5;
                break;
        }
        if (opcode != NOP)
            return new InsnNode(opcode);
        else if (n >= Byte.MIN_VALUE && n <= Byte.MAX_VALUE)
            return new IntInsnNode(BIPUSH, n);
        else if (n >= Short.MIN_VALUE && n <= Short.MAX_VALUE)
            return new IntInsnNode(SIPUSH, n);
        return new LdcInsnNode(n);
    }

    static Object newLoadThis() {
        return new VarInsnNode(ALOAD, 0);
    }

    static Object newInvokeVirtual(Method method) {
        return newInvoke(method, INVOKEVIRTUAL);
    }

    static Object newInvokeVirtual(String classInternalName, String methodName, String methodDesc) {
        return new MethodInsnNode(
                INVOKEVIRTUAL,
                classInternalName,
                methodName,
                methodDesc,
                false
        );
    }

    static Object newInvokeStatic(Method method) {
        return newInvoke(method, INVOKESTATIC);
    }

    static Object newInvokeStatic(String classInternalName, String methodName, String methodDesc) {
        return new MethodInsnNode(
                INVOKESTATIC,
                classInternalName,
                methodName,
                methodDesc,
                false
        );
    }

    static Object newGetStaticField(Field field) {
        return new FieldInsnNode(
                GETSTATIC,
                Type.getType(
                        field.getDeclaringClass()
                ).getInternalName(),
                field.getName(),
                Type.getType(
                        field.getType()
                ).getDescriptor()
        );
    }

    private static Object newInvoke(Method method, int opcode) {
        return new MethodInsnNode(
                opcode,
                Type.getInternalName(
                        method.getDeclaringClass()
                ),
                method.getName(),
                Type.getMethodDescriptor(
                        method
                ),
                false
        );
    }

    static Object newPop() {
        return new InsnNode(POP);
    }

    static Object newCast(String desc) {
        return new TypeInsnNode(
                CHECKCAST,
                Type.getType(desc).getInternalName()
        );
    }

    static Object mayCastToReturnType(Type returnType) {
        if (returnType.getSort() == Type.VOID ||
                returnType.getClassName().equals(Object.class.getName()))
            return null;
        String returnDesc = returnType.getDescriptor();
        Object unwrapCallNode = PrimitiveWrapper.mayCreateUnwrapCallNode(returnDesc);
        return unwrapCallNode == null ?
                newCast(returnDesc) :
                new Object[]{
                        newCast(
                                Optional.ofNullable(
                                        PrimitiveWrapper.getWrapperType(returnDesc)
                                ).orElseThrow(
                                        () -> new RuntimeException("No wrapper type found for primitive desc: " + returnDesc)
                                ).getDescriptor()
                        ),
                        unwrapCallNode
                };
    }

    interface ValueConverter<T> {
        Object convert(T node);
    }

    interface AsmFunc {
        Object run();
    }
}
