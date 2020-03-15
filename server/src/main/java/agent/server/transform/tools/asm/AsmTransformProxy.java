package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.impl.invoke.ConstructorInvoke;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import static agent.server.transform.tools.asm.AsmMethod.*;
import static org.objectweb.asm.Opcodes.*;

public class AsmTransformProxy {
    static byte[] transform(Class<?> sourceClass, byte[] classData, Map<Integer, DestInvoke> idToInvoke) {
        return AsmUtils.transformClass(
                sourceClass,
                classData,
                classNode -> doTransform(classNode, idToInvoke)
        );
    }

    private static void doTransform(ClassNode classNode, Map<Integer, DestInvoke> idToInvoke) {
        Map<String, Map<String, Integer>> nameToDescToId = getNameToDescToId(idToInvoke);
        for (MethodNode methodNode : classNode.methods) {
            Integer id = findInvokeId(methodNode, nameToDescToId);
            if (id != null) {
                DestInvoke destInvoke = Optional.ofNullable(
                        idToInvoke.get(id)
                ).orElseThrow(
                        () -> new RuntimeException("No dest invoke found by id: " + id)
                );
                transformInvoke(methodNode, id, destInvoke);
            }
        }
    }

    private static Map<String, Map<String, Integer>> getNameToDescToId(Map<Integer, DestInvoke> idToInvoke) {
        Map<String, Map<String, Integer>> nameToDescToId = new HashMap<>();
        idToInvoke.forEach(
                (id, destInvoke) -> nameToDescToId.computeIfAbsent(
                        destInvoke.getName(),
                        key -> new HashMap<>()
                ).put(
                        destInvoke.getDescriptor(),
                        id
                )
        );
        return nameToDescToId;
    }

    private static Integer findInvokeId(MethodNode methodNode, Map<String, Map<String, Integer>> nameToDescToId) {
        Map<String, Integer> descToId = nameToDescToId.get(methodNode.name);
        return descToId != null ?
                descToId.get(methodNode.desc) :
                null;
    }

    private static void transformInvoke(MethodNode methodNode, int invokeId, DestInvoke destInvoke) {
        switch (destInvoke.getType()) {
            case CONSTRUCTOR:
                transformConstructorInvoke(methodNode, invokeId, (ConstructorInvoke) destInvoke);
                break;
            case METHOD:
                transformMethodInvoke(methodNode, invokeId, (MethodInvoke) destInvoke, false);
                break;
            default:
                throw new IllegalArgumentException("Unknown dest invoke type: " + destInvoke.getType());
        }
    }

    private static void transformConstructorInvoke(MethodNode methodNode, int invokeId, ConstructorInvoke invoke) {
        int newLocalIdx = methodNode.maxLocals;
        ListIterator<AbstractInsnNode> iter = methodNode.instructions.iterator();
        while (iter.hasNext()) {
            AbstractInsnNode node = iter.next();
            int opcode = node.getOpcode();
            if (opcode == RETURN) {
                methodNode.instructions.insertBefore(
                        node,
                        newVoidReturnList(false, invokeId)
                );
            } else if (opcode == ATHROW) {
                methodNode.instructions.insertBefore(
                        node,
                        newThrowList(false, invokeId, newLocalIdx)
                );
            }
        }
        methodNode.instructions.insert(
                newBeforeList(invokeId, invoke, true)
        );
    }

    private static void transformMethodInvoke(MethodNode methodNode, int invokeId, MethodInvoke invoke, boolean weaveInnerCalls) {
        boolean isStatic = invoke.isStatic();
        int newLocalIdx = methodNode.maxLocals;
        int innerCallLocalIdx = -1;
        if (weaveInnerCalls) {
            innerCallLocalIdx = newLocalIdx;
            newLocalIdx += Type.LONG_TYPE.getSize();
        }
        ListIterator<AbstractInsnNode> iter = methodNode.instructions.iterator();
        LabelNode startLabelNode = null;
        while (iter.hasNext()) {
            AbstractInsnNode node = iter.next();
            int opcode = node.getOpcode();
            if (opcode >= IRETURN && opcode < RETURN) {
                methodNode.instructions.insertBefore(
                        node,
                        newValueReturnList(
                                (Method) invoke.getInvokeEntity(),
                                invokeId,
                                newLocalIdx
                        )
                );
            } else if (opcode == RETURN) {
                methodNode.instructions.insertBefore(
                        node,
                        newVoidReturnList(isStatic, invokeId)
                );
            } else if (node.getType() == AbstractInsnNode.LABEL) {
                if (startLabelNode == null)
                    startLabelNode = (LabelNode) node;
            } else if (weaveInnerCalls && isInvoke(opcode)) {
                if (isInvokeDynamic(opcode)) {
                    // TODO
                } else {
                    methodNode.instructions.insertBefore(
                            node,
                            newBeforeInnerCall(
                                    (MethodInsnNode) node,
                                    innerCallLocalIdx,
                                    newLocalIdx
                            )
                    );
                    methodNode.instructions.insertBefore(
                            node.getNext(),
                            newAfterInnerCall(
                                    (MethodInsnNode) node,
                                    innerCallLocalIdx,
                                    newLocalIdx
                            )
                    );
                }
            }
        }
        methodNode.instructions.insert(
                newBeforeList(invokeId, invoke, false)
        );
        if (weaveInnerCalls)
            methodNode.instructions.insert(
                    newInitInnerCallNum(innerCallLocalIdx)
            );
        if (startLabelNode != null)
            processTryCatch(isStatic, methodNode, startLabelNode, invokeId, newLocalIdx);
    }

    private static InsnList newInitInnerCallNum(int innerCallIdx) {
        return addTo(
                new InsnList(),
                initLong(0, innerCallIdx)
        );
    }

    private static String getMethodFullDesc(MethodInsnNode node) {
        return node.owner + "#" + node.name + node.desc;
    }

    private static InsnList newBeforeInnerCall(MethodInsnNode node, int innerCallIdx, int newLocalIdx) {
        Type[] argTypes = Type.getMethodType(node.desc).getArgumentTypes();
        Type[] reversedTypes = Utils.reverse(argTypes);
        return addTo(
                new InsnList(),
                updateLong(1, innerCallIdx),
                storeByTypes(reversedTypes, newLocalIdx),
                newGetInstance(),
                loadLongType(innerCallIdx),
                loadLdc(
                        getMethodFullDesc(node)
                ),
                loadArgArray(
                        Utils.reverse(
                                newParamObjects(reversedTypes, newLocalIdx)
                        )
                ),
                newOnBeforeInnerCallMethod(),
                Utils.reverse(
                        loadByTypes(reversedTypes, newLocalIdx)
                )
        );
    }

    private static InsnList newAfterInnerCall(MethodInsnNode node, int innerCallIdx, int newLocalIdx) {
        Type returnType = Type.getReturnType(node.desc);
        return returnType.getSort() == Type.VOID ?
                addTo(
                        new InsnList(),
                        newGetInstance(),
                        loadLongType(innerCallIdx),
                        loadNull(1),
                        newOnAfterInnerCallMethod()
                ) :
                addTo(
                        new InsnList(),
                        storeByType(returnType, newLocalIdx),
                        newGetInstance(),
                        loadLongType(innerCallIdx),
                        loadMayWrapPrimitive(returnType, newLocalIdx),
                        newOnAfterInnerCallMethod(),
                        loadByType(returnType, newLocalIdx)
                );
    }

    public static boolean isInvoke(int opcode) {
        return opcode == INVOKEVIRTUAL ||
                opcode == INVOKESPECIAL ||
                opcode == INVOKESTATIC ||
                opcode == INVOKEINTERFACE ||
                opcode == INVOKEDYNAMIC;
    }

    public static boolean isInvokeDynamic(int opcode) {
        return opcode == INVOKEDYNAMIC;
    }

    private static void processTryCatch(boolean isStatic, MethodNode methodNode, LabelNode startLabelNode, int invokeId, int newLocalIdx) {
        LabelNode handlerLabelNode = newLabel();
        TryCatchBlockNode tryCatchNode = newTryCatch(
                startLabelNode,
                handlerLabelNode,
                handlerLabelNode,
                null
        );
        methodNode.tryCatchBlocks.add(tryCatchNode);
        addTo(
                methodNode.instructions,
                handlerLabelNode,
                newThrowList(isStatic, invokeId, newLocalIdx),
                newThrow()
        );
    }

    private static InsnList newBeforeList(int invokeId, DestInvoke invoke, boolean unInit) {
        return addTo(
                new InsnList(),
                newGetInstance(),
                loadInt(invokeId),
                aloadThisOrNull(
                        unInit || invoke.isStatic()
                ),
                collectArgs(
                        invoke.getParamTypes(),
                        invoke.isStatic() ? 0 : 1
                ),
                newOnBeforeMethod()
        );
    }

    private static InsnList newValueReturnList(Method method, int invokeId, int newLocalIdx) {
        Type returnType = getReturnType(method);
        return addTo(
                new InsnList(),
                storeByType(returnType, newLocalIdx),
                newGetInstance(),
                loadInt(invokeId),
                aloadThisOrNull(method),
                loadMayWrapPrimitive(returnType, newLocalIdx),
                newOnReturningMethod(),
                loadByType(returnType, newLocalIdx)
        );
    }

    private static InsnList newVoidReturnList(boolean isStatic, int invokeId) {
        return addTo(
                new InsnList(),
                newGetInstance(),
                loadInt(invokeId),
                aloadThisOrNull(isStatic),
                loadNull(1),
                newOnReturningMethod()
        );
    }

    private static InsnList newThrowList(boolean isStatic, int invokeId, int newLocalIdx) {
        return addTo(
                new InsnList(),
                astore(newLocalIdx),
                newGetInstance(),
                loadInt(invokeId),
                aloadThisOrNull(isStatic),
                aload(newLocalIdx),
                newOnThrowingMethod(),
                aload(newLocalIdx)
        );
    }

    private static Object collectArgs(Method method) {
        return loadArgArray(
                getParamObjects(
                        method,
                        ReflectionUtils.isStatic(method) ? 0 : 1
                )
        );
    }

    private static Object collectArgs(Class[] paramTypes, int startIdx) {
        return loadArgArray(
                getParamObjects(paramTypes, startIdx)
        );
    }

    private static Object newGetInstance() {
        return invokeStatic(
                findMethod("getInstance")
        );
    }

    private static Object newProxyMethod(String methodName) {
        return invokeVirtual(
                findMethod(methodName)
        );
    }

    private static Object newOnBeforeMethod() {
        return newProxyMethod("onBefore");
    }

    private static Object newOnReturningMethod() {
        return newProxyMethod("onReturning");
    }

    private static Object newOnThrowingMethod() {
        return newProxyMethod("onThrowing");
    }

    private static Object newOnBeforeInnerCallMethod() {
        return newProxyMethod("onBeforeInnerCall");
    }

    private static Object newOnAfterInnerCallMethod() {
        return newProxyMethod("onAfterInnerCall");
    }

    private static Method findMethod(String methodName) {
        return Optional.ofNullable(
                Utils.wrapToRtError(
                        () ->
                                ReflectionUtils.findFirstMethod(ProxyTransformMgr.class, methodName)
                )
        ).orElseThrow(
                () -> new RuntimeException("No method found by name: " + methodName)
        );
    }

}
