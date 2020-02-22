package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.impl.invoke.DestInvoke;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;

import static agent.server.transform.tools.asm.AsmMethod.*;
import static org.objectweb.asm.Opcodes.*;

class AsmTransformProxy {
    static byte[] transform(byte[] classData, Map<Integer, DestInvoke> idToInvoke) {
        return AsmUtils.transform(
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
                transformConstructorInvoke(methodNode, invokeId, (Constructor) destInvoke.getInvokeEntity());
                break;
            case METHOD:
                transformMethodInvoke(methodNode, invokeId, (Method) destInvoke.getInvokeEntity(), true);
                break;
            default:
                throw new IllegalArgumentException("Unknown dest invoke type: " + destInvoke.getType());
        }
    }

    private static void transformConstructorInvoke(MethodNode methodNode, int invokeId, Constructor constructor) {
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
    }

    private static void transformMethodInvoke(MethodNode methodNode, int invokeId, Method method, boolean weaveInnerCalls) {
        boolean isStatic = isStatic(method);
        int newLocalIdx = methodNode.maxLocals;
        ListIterator<AbstractInsnNode> iter = methodNode.instructions.iterator();
        LabelNode startLabelNode = null;
        while (iter.hasNext()) {
            AbstractInsnNode node = iter.next();
            int opcode = node.getOpcode();
            if (opcode >= IRETURN && opcode < RETURN) {
                methodNode.instructions.insertBefore(
                        node,
                        newValueReturnList(method, invokeId, newLocalIdx)
                );
            } else if (opcode == RETURN) {
                methodNode.instructions.insertBefore(
                        node,
                        newVoidReturnList(isStatic, invokeId)
                );
            } else if (node.getType() == AbstractInsnNode.LABEL) {
                if (startLabelNode == null)
                    startLabelNode = (LabelNode) node;
            } else if (weaveInnerCalls && isMethodCall(opcode)) {
                methodNode.instructions.insertBefore(
                        node,
                        newBeforeInnerCall(
                                (MethodInsnNode) node,
                                newLocalIdx
                        )
                );
                methodNode.instructions.insertBefore(
                        node.getNext(),
                        newAfterInnerCall(
                                (MethodInsnNode) node,
                                newLocalIdx
                        )
                );
            }
        }
        methodNode.instructions.insert(
                newBeforeList(invokeId, method)
        );
        if (startLabelNode != null)
            processTryCatch(isStatic, methodNode, startLabelNode, invokeId, newLocalIdx);
    }

    private static String getMethodFullDesc(MethodInsnNode node) {
        return node.owner + "#" + node.name + node.desc;
    }

    private static InsnList newBeforeInnerCall(MethodInsnNode node, int newLocalIdx) {
        Type[] argTypes = Type.getMethodType(node.desc).getArgumentTypes();
        Type[] reversedTypes = Utils.reverse(argTypes);
        return addTo(
                new InsnList(),
                newStore(reversedTypes, newLocalIdx),
                newGetInstance(),
                newLoadLdc(
                        getMethodFullDesc(node)
                ),
                newLoadArgArray(
                        Utils.reverse(
                                newParamObjects(reversedTypes, newLocalIdx)
                        )
                ),
                newOnBeforeInnerCallMethod(),
                Utils.reverse(
                        newLoad(reversedTypes, newLocalIdx)
                )
        );
    }

    private static InsnList newAfterInnerCall(MethodInsnNode node, int newLocalIdx) {
        Type returnType = Type.getReturnType(node.desc);
        return returnType.getSort() == Type.VOID ?
                addTo(
                        new InsnList(),
                        newGetInstance(),
                        newLoadNull(1),
                        newOnAfterInnerCallMethod()
                ) :
                addTo(
                        new InsnList(),
                        newStore(returnType, newLocalIdx),
                        newGetInstance(),
                        newLoadWrapPrimitive(returnType, newLocalIdx),
                        newOnAfterInnerCallMethod(),
                        newLoad(returnType, newLocalIdx)
                );
    }

    private static boolean isMethodCall(int opcode) {
        return opcode == INVOKEVIRTUAL ||
                opcode == INVOKESPECIAL ||
                opcode == INVOKESTATIC ||
                opcode == INVOKEINTERFACE;
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

    private static InsnList newBeforeList(int invokeId, Method method) {
        return addTo(
                new InsnList(),
                newGetInstance(),
                newNumLoad(invokeId),
                newLoadThisOrNull(method),
                collectArgs(method),
                newOnBeforeMethod()
        );
    }

    private static InsnList newValueReturnList(Method method, int invokeId, int newLocalIdx) {
        Type returnType = getReturnType(method);
        return addTo(
                new InsnList(),
                newStore(returnType, newLocalIdx),
                newGetInstance(),
                newNumLoad(invokeId),
                newLoadThisOrNull(method),
                newLoadWrapPrimitive(returnType, newLocalIdx),
                newOnReturningMethod(),
                newLoad(returnType, newLocalIdx)
        );
    }

    private static InsnList newVoidReturnList(boolean isStatic, int invokeId) {
        return addTo(
                new InsnList(),
                newGetInstance(),
                newNumLoad(invokeId),
                newLoadThisOrNull(isStatic),
                newLoadNull(1),
                newOnReturningMethod()
        );
    }

    private static InsnList newThrowList(boolean isStatic, int invokeId, int newLocalIdx) {
        return addTo(
                new InsnList(),
                newAStore(newLocalIdx),
                newGetInstance(),
                newNumLoad(invokeId),
                newLoadThisOrNull(isStatic),
                newALoad(newLocalIdx),
                newOnThrowingMethod(),
                newALoad(newLocalIdx)
        );
    }

    private static Object collectArgs(Method method) {
        return newLoadArgArray(
                getParamObjects(
                        method,
                        isStatic(method) ? 0 : 1
                )
        );
    }

    private static Object newGetInstance() {
        return newInvokeStatic(
                findMethod("getInstance")
        );
    }

    private static Object newProxyMethod(String methodName) {
        return newInvokeVirtual(
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
