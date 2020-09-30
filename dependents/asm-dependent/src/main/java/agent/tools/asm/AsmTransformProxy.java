package agent.tools.asm;

import agent.base.utils.Pair;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.invoke.ConstructorInvoke;
import agent.invoke.DestInvoke;
import agent.invoke.MethodInvoke;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static agent.tools.asm.AsmMethod.*;
import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.tree.AbstractInsnNode.*;

class AsmTransformProxy {
    private static final String TARGET_PROXY_CLASS = "agent.bootstrap.ProxyDelegate";

    static void doTransform(ClassNode classNode, Map<Integer, DestInvoke> idToInvoke) {
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
        Map<Label, Integer> labelToIdx = getLabelIndexMap(methodNode);
        List<Pair<Integer, Integer>> ranges = getTryCatchRanges(methodNode, labelToIdx);
        int newLocalIdx = methodNode.maxLocals;
        for (AbstractInsnNode node : methodNode.instructions) {
            int opcode = node.getOpcode();
            if (opcode == RETURN) {
                methodNode.instructions.insertBefore(
                        node,
                        newVoidReturnList(true, invokeId)
                );
            } else if (opcode == ATHROW &&
                    !isInCatchBlock(node, labelToIdx, ranges)) {
                methodNode.instructions.insertBefore(
                        node,
                        newThrowList(false, invokeId, newLocalIdx)
                );
            }
        }
        methodNode.instructions.insert(
                newBeforeList(invokeId, invoke, true)
        );
        processTryCatch(false, methodNode, invokeId, newLocalIdx);
    }

    private static boolean isInCatchBlock(AbstractInsnNode node, Map<Label, Integer> labelToIdx, List<Pair<Integer, Integer>> ranges) {
        Label label = getRelativeLabel(node);
        if (label == null)
            return false;
        int idx = getLabelIdx(labelToIdx, label);
        return ranges.stream().anyMatch(
                p -> p.left <= idx && p.right > idx
        );
    }

    private static Label getRelativeLabel(AbstractInsnNode node) {
        if (node == null)
            return null;
        if (node.getType() == LABEL)
            return ((LabelNode) node).getLabel();
        return getRelativeLabel(
                node.getPrevious()
        );
    }

    private static List<Pair<Integer, Integer>> getTryCatchRanges(MethodNode methodNode, Map<Label, Integer> labelToIdx) {
        return methodNode.tryCatchBlocks == null ?
                Collections.emptyList() :
                methodNode.tryCatchBlocks.stream()
                        .map(
                                tryCatchBlockNode -> new Pair<>(
                                        getLabelIdx(
                                                labelToIdx,
                                                tryCatchBlockNode.start.getLabel()
                                        ),
                                        getLabelIdx(
                                                labelToIdx,
                                                tryCatchBlockNode.end.getLabel()
                                        )
                                )
                        )
                        .collect(
                                Collectors.toList()
                        );
    }

    private static int getLabelIdx(Map<Label, Integer> labelToIdx, Label label) {
        Integer idx = labelToIdx.get(label);
        if (idx == null)
            throw new RuntimeException("No index found for label: " + label);
        return idx;
    }

    private static Map<Label, Integer> getLabelIndexMap(MethodNode methodNode) {
        Map<Label, Integer> rs = new HashMap<>();
        int idx = 0;
        for (AbstractInsnNode node : methodNode.instructions) {
            if (node.getType() == LABEL)
                rs.put(
                        ((LabelNode) node).getLabel(),
                        idx++
                );
        }
        return rs;
    }

    private static void transformMethodInvoke(MethodNode methodNode, int invokeId, MethodInvoke invoke, boolean weaveInnerCalls) {
        boolean useNull = invoke.isStatic();
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
                        newVoidReturnList(useNull, invokeId)
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
        processTryCatch(useNull, methodNode, invokeId, newLocalIdx);
        if (startLabelNode != null)
            addTryCatchForFunc(useNull, methodNode, startLabelNode, invokeId, newLocalIdx);
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

    private static boolean isInvoke(int opcode) {
        return opcode == INVOKEVIRTUAL ||
                opcode == INVOKESPECIAL ||
                opcode == INVOKESTATIC ||
                opcode == INVOKEINTERFACE ||
                opcode == INVOKEDYNAMIC;
    }

    private static boolean isInvokeDynamic(int opcode) {
        return opcode == INVOKEDYNAMIC;
    }

    private static void processTryCatch(boolean useNull, MethodNode methodNode, int invokeId, int newLocalIdx) {
        if (methodNode.tryCatchBlocks != null) {
            Set<String> rangeSet = new HashSet<>();
            methodNode.tryCatchBlocks.forEach(
                    tryCatchBlockNode -> {
                        if (tryCatchBlockNode.type != null) {
                            // for catch multi errors (FileNotFoundException | IllegalArgumentException | NoRouteToHostException)
                            String range = tryCatchBlockNode.start.getLabel() + "-" +
                                    tryCatchBlockNode.end.getLabel() + "-" +
                                    tryCatchBlockNode.handler.getLabel();
                            if (!rangeSet.contains(range)) {
                                AbstractInsnNode node = tryCatchBlockNode.handler.getNext();
                                while (node != null) {
                                    int nodeType = node.getType();
                                    if (nodeType != LINE && nodeType != FRAME)
                                        break;
                                    node = node.getNext();
                                }
                                if (node != null)
                                    methodNode.instructions.insertBefore(
                                            node,
                                            newCatching(useNull, invokeId, newLocalIdx)
                                    );
                                rangeSet.add(range);
                            }
                        }
                    }
            );
        }
    }

    private static void addTryCatchForFunc(boolean useNull, MethodNode methodNode, LabelNode startLabelNode, int invokeId, int newLocalIdx) {
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
                newThrowList(useNull, invokeId, newLocalIdx),
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

    private static InsnList newVoidReturnList(boolean useNull, int invokeId) {
        return addTo(
                new InsnList(),
                newGetInstance(),
                loadInt(invokeId),
                aloadThisOrNull(useNull),
                loadNull(1),
                newOnReturningMethod()
        );
    }

    private static InsnList newThrowList(boolean useNull, int invokeId, int newLocalIdx) {
        return newErrorList(
                useNull,
                invokeId,
                newLocalIdx,
                newOnThrowingMethod()
        );
    }

    private static InsnList newCatching(boolean useNull, int invokeId, int newLocalIdx) {
        return newErrorList(
                useNull,
                invokeId,
                newLocalIdx,
                newOnCatchingMethod()
        );
    }

    private static InsnList newErrorList(boolean useNull, int invokeId, int newLocalIdx, Object errorProcessor) {
        return addTo(
                new InsnList(),
                astore(newLocalIdx),
                newGetInstance(),
                loadInt(invokeId),
                aloadThisOrNull(useNull),
                aload(newLocalIdx),
                errorProcessor,
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

    private static Object newOnCatchingMethod() {
        return newProxyMethod("onCatching");
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
                        () -> ReflectionUtils.findFirstMethod(TARGET_PROXY_CLASS, methodName)
                )
        ).orElseThrow(
                () -> new RuntimeException("No method found by name: " + methodName)
        );
    }
}
