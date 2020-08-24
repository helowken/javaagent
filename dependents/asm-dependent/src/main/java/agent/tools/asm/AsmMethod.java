package agent.tools.asm;

import agent.base.utils.ReflectionUtils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.objectweb.asm.Opcodes.*;

class AsmMethod {

    static Type getReturnType(Method method) {
        return Type.getReturnType(method);
    }

    static Type getReturnType(MethodNode methodNode) {
        return Type.getReturnType(
                methodNode.desc
        );
    }

    static boolean isVoid(MethodNode methodNode) {
        return getReturnType(methodNode).getSort() == Type.VOID;
    }

    static InsnList addTo(InsnList insnList, Object... ns) {
        if (ns != null) {
            for (Object n : ns) {
                if (n instanceof InsnList)
                    insnList.add((InsnList) n);
                else if (n instanceof AbstractInsnNode)
                    insnList.add((AbstractInsnNode) n);
                else if (n instanceof Object[])
                    addTo(insnList, (Object[]) n);
                else if (n instanceof Collection)
                    addTo(insnList, ((Collection) n).toArray());
                else
                    throw new IllegalArgumentException("Invalid argument: " + n);
            }
        }
        return insnList;
    }

    static Object loadByDesc(String desc, int index) {
        return loadByType(
                Type.getType(desc),
                index
        );
    }

    static Object loadByClass(Class<?> clazz, int index) {
        return loadByType(
                Type.getType(clazz),
                index
        );
    }

    static Object loadByType(Type type, int index) {
        return new VarInsnNode(
                type.getOpcode(ILOAD),
                index
        );
    }

    static List<Object> loadByTypes(Type[] types, int startIdx) {
        List<Object> rsList = new ArrayList<>();
        int idx = startIdx;
        for (Type type : types) {
            rsList.add(
                    loadByType(type, idx)
            );
            idx += type.getSize();
        }
        return rsList;
    }

    static Object loadLongType(int index) {
        return loadByType(Type.LONG_TYPE, index);
    }

    static List<ParamObject> getParamObjects(Method method, int startIdx) {
        return getParamObjects(
                method.getParameterTypes(),
                startIdx
        );
    }

    static List<ParamObject> getParamObjects(Class[] paramTypes, int startIdx) {
        return newParamObjects(
                Stream.of(paramTypes)
                        .map(Type::getType)
                        .toArray(Type[]::new),
                startIdx
        );
    }

    static List<ParamObject> newParamObjects(Type[] argTypes, int startIdx) {
        int size = argTypes.length;
        List<ParamObject> paramObjects = new ArrayList<>(size);
        int idx = startIdx;
        for (Type argType : argTypes) {
            paramObjects.add(
                    new ParamObject(argType, idx)
            );
            idx += argType.getSize();
        }
        return paramObjects;
    }

    static Object loadArgArray(List<ParamObject> poList) {
        return poList.isEmpty() ?
                loadNull(1) :
                new Object[]{
                        newObjectArray(
                                poList.size()
                        ),
                        populateObjectArray(
                                poList,
                                AsmMethod::loadMayWrapPrimitive
                        )
                };
    }

    static Object loadMayWrapPrimitive(ParamObject po) {
        return loadMayWrapPrimitive(po.type, po.index);
    }

    static Object loadMayWrapPrimitive(Type type, int index) {
        Object load = loadByType(type, index);
        Object wrapCall = PrimitiveWrapper.mayCreateWrapCallNode(
                type.getDescriptor()
        );
        return wrapCall != null ?
                new Object[]{load, wrapCall} :
                load;
    }

    static Object loadClassName(String desc) {
        return loadLdc(
                Type.getType(desc).getClassName()
        );
    }

    static Object loadClass(String internalName) {
        return loadLdc(
                Type.getType(internalName)
        );
    }

    static Object loadLdc(Object value) {
        return new LdcInsnNode(value);
    }

    static Object loadNull(int count) {
        List<Object> rsList = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            rsList.add(
                    new InsnNode(ACONST_NULL)
            );
        }
        return rsList;
    }

    static Object aload(int index) {
        return new VarInsnNode(ALOAD, index);
    }

    static Object astore(int index) {
        return new VarInsnNode(ASTORE, index);
    }

    static Object storeByType(Type type, int index) {
        return new VarInsnNode(
                type.getOpcode(ISTORE),
                index
        );
    }

    static Object storeByTypes(Type[] types, int startIdx) {
        List<Object> rsList = new ArrayList<>();
        int idx = startIdx;
        for (Type type : types) {
            rsList.add(
                    storeByType(type, idx)
            );
            idx += type.getSize();
        }
        return rsList;
    }

    static Object storeLongType(int index) {
        return storeByType(Type.LONG_TYPE, index);
    }

    static Object newThrow() {
        return new InsnNode(ATHROW);
    }

    static Object newReturnByDesc(String desc) {
        return newReturnByType(
                Type.getType(desc).getReturnType()
        );
    }

    static Object newReturnByType(Type returnType) {
        return new InsnNode(
                returnType.getOpcode(IRETURN)
        );
    }

    static TryCatchBlockNode newTryCatch() {
        return newTryCatch(
                new LabelNode(),
                new LabelNode(),
                new LabelNode(),
                null
        );
    }

    static TryCatchBlockNode newTryCatch(LabelNode start, LabelNode end, LabelNode handler, String exceptionType) {
        return new TryCatchBlockNode(start, end, handler, exceptionType);
    }

    static LabelNode newLabel() {
        return new LabelNode();
    }

    static Object newArray(Class<?> clazz, int len) {
        return new Object[]{
                loadInt(len),
                new TypeInsnNode(
                        ANEWARRAY,
                        Type.getInternalName(clazz)
                )
        };
    }

    static Object newObjectArray(int len) {
        return newArray(Object.class, len);
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

    static <T> Object populateObjectArray(List<T> nodes, ValueConverter<T> arrayValueSupplier) {
        return populateArray(Object.class, nodes, arrayValueSupplier);
    }

    static Object setValueToArray(Class<?> clazz, int index, Object value) {
        return new Object[]{
                new InsnNode(DUP),
                loadInt(index),
                value,
                new InsnNode(
                        Type.getType(clazz).getOpcode(IASTORE)
                )
        };
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
                        ReflectionUtils.CONSTRUCTOR_NAME,
                        Type.getType(constructor).getDescriptor(),
                        false
                )
        };
    }

    static Object initLong(long n, int index) {
        return new Object[]{
                loadLong(n),
                storeLongType(index)
        };
    }

    static Object updateLong(long value, int index) {
        return new Object[]{
                loadLongType(index),
                loadLong(value),
                new InsnNode(LADD),
                storeLongType(index)
        };
    }

    static Object loadLong(long n) {
        if (n == 0)
            return new InsnNode(LCONST_0);
        if (n == 1)
            return new InsnNode(LCONST_1);
        return loadLdc(n);
    }

    static Object loadInt(int n) {
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

    static Object aloadThis() {
        return aload(0);
    }

    static Object invokeVirtual(Method method) {
        return newInvoke(method, INVOKEVIRTUAL);
    }

    static Object invokeVirtual(String classInternalName, String methodName, String methodDesc) {
        return new MethodInsnNode(
                INVOKEVIRTUAL,
                classInternalName,
                methodName,
                methodDesc,
                false
        );
    }

    static Object invokeStatic(Method method) {
        return newInvoke(method, INVOKESTATIC);
    }

    static Object invokeStatic(String classInternalName, String methodName, String methodDesc) {
        return new MethodInsnNode(
                INVOKESTATIC,
                classInternalName,
                methodName,
                methodDesc,
                false
        );
    }

    static Object getStaticField(Field field) {
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

    static Object pop() {
        return new InsnNode(POP);
    }

    static Object aloadThisOrNull(Method method) {
        return aloadThisOrNull(
                ReflectionUtils.isStatic(method)
        );
    }

    static Object aloadThisOrNull(boolean isStatic) {
        return isStatic ?
                loadNull(1) :
                aloadThis();
    }

    static Object dup() {
        return new InsnNode(DUP);
    }

    static Object castByDesc(String desc) {
        return new TypeInsnNode(
                CHECKCAST,
                Type.getType(desc).getInternalName()
        );
    }

    static Object castForReturnType(Type returnType) {
        if (returnType.getSort() == Type.VOID ||
                returnType.getClassName().equals(Object.class.getName()))
            return null;
        String returnDesc = returnType.getDescriptor();
        Object unwrapCallNode = PrimitiveWrapper.mayCreateUnwrapCallNode(returnDesc);
        return unwrapCallNode == null ?
                castByDesc(returnDesc) :
                new Object[]{
                        castByDesc(
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

    static class ParamObject {
        final Type type;
        final int index;

        private ParamObject(Type type, int index) {
            this.type = type;
            this.index = index;
        }
    }
}
