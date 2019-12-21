package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;

import static agent.server.transform.tools.asm.AsmMethod.*;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

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
                transformMethodInvoke(methodNode, invokeId, (Method) destInvoke.getInvokeEntity());
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
                        newThrowList(false,invokeId, newLocalIdx)
                );
            }
        }
    }

    private static void transformMethodInvoke(MethodNode methodNode, int invokeId, Method method) {
        boolean isStatic = isStatic(method);
        int newLocalIdx = methodNode.maxLocals;
        methodNode.instructions.insert(
                newBeforeList(invokeId, method)
        );
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
            }
        }
        if (startLabelNode != null)
            processTryCatch(isStatic, methodNode, startLabelNode, invokeId, newLocalIdx);
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
                newInvokeStatic(
                        getGetInstanceMethod()
                ),
                newNumLoad(invokeId),
                newLoadThisOrNull(method),
                collectArgs(method),
                newInvokeVirtual(
                        getOnBeforeMethod()
                )
        );
    }

    private static InsnList newValueReturnList(Method method, int invokeId, int newLocalIdx) {
        Type returnType = getReturnType(method);
        return addTo(
                new InsnList(),
                newStore(returnType, newLocalIdx),
                newInvokeStatic(
                        getGetInstanceMethod()
                ),
                newNumLoad(invokeId),
                newLoadThisOrNull(method),
                newLoadWrapPrimitive(returnType, newLocalIdx),
                newInvokeVirtual(
                        getOnReturningMethod()
                ),
                newLoad(returnType, newLocalIdx)
        );
    }

    private static InsnList newVoidReturnList(boolean isStatic, int invokeId) {
        return addTo(
                new InsnList(),
                newInvokeStatic(
                        getGetInstanceMethod()
                ),
                newNumLoad(invokeId),
                newLoadThisOrNull(isStatic),
                newLoadNull(1),
                newInvokeVirtual(
                        getOnReturningMethod()
                )
        );
    }

    private static InsnList newThrowList(boolean isStatic, int invokeId, int newLocalIdx) {
        return addTo(
                new InsnList(),
                newAStore(newLocalIdx),
                newInvokeStatic(
                        getGetInstanceMethod()
                ),
                newNumLoad(invokeId),
                newLoadThisOrNull(isStatic),
                newALoad(newLocalIdx),
                newInvokeVirtual(
                        getOnThrowingMethod()
                ),
                newALoad(newLocalIdx)
        );
    }

    private static Object collectArgs(Method method) {
        List<ParamObject> poList = getParamObjects(method);
        return poList.isEmpty() ?
                newLoadNull(1) :
                new Object[]{
                        newArray(
                                Object.class,
                                poList.size()
                        ),
                        populateArray(
                                Object.class,
                                poList,
                                AsmMethod::newLoadWrapPrimitive
                        )
                };
    }

    private static Method getOnBeforeMethod() {
        return findMethod("onBefore");
    }

    private static Method getOnReturningMethod() {
        return findMethod("onReturning");
    }

    private static Method getOnThrowingMethod() {
        return findMethod("onThrowing");
    }

    private static Method getGetInstanceMethod() {
        return findMethod("getInstance");
    }

    private static Method findMethod(String methodName) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.findFirstMethod(ProxyTransformMgr.class, methodName)
        );
    }

}
