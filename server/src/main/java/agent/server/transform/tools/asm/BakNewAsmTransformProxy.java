package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static agent.server.transform.tools.asm.AsmMethod.*;
import static org.objectweb.asm.Opcodes.RETURN;

class BakNewAsmTransformProxy {
//    private static final AtomicLong nameIdGenerator = new AtomicLong(0);
//
//    static byte[] transform(byte[] classData, Map<Integer, DestInvoke> idToInvoke) {
//        return AsmUtils.transform(
//                classData,
//                classNode -> doTransform(classNode, idToInvoke)
//        );
//    }
//
//    private static void doTransform(ClassNode classNode, Map<Integer, DestInvoke> idToInvoke) {
//        Map<String, Map<String, Integer>> nameToDescToId = getNameToDescToId(idToInvoke);
//        List<MethodNode> methodNodes = new ArrayList<>(classNode.methods);
//        Set<String> invokeNames = new HashSet<>();
//        methodNodes.forEach(
//                methodNode -> invokeNames.add(methodNode.name)
//        );
//        for (MethodNode methodNode : methodNodes) {
//            Integer id = findInvokeId(methodNode, nameToDescToId);
//            if (id != null) {
//                DestInvoke destInvoke = Optional.ofNullable(
//                        idToInvoke.get(id)
//                ).orElseThrow(
//                        () -> new RuntimeException("No dest invoke found by id: " + id)
//                );
//                transformInvoke(classNode, methodNode, id, destInvoke, invokeNames);
//            }
//        }
//    }
//
//    private static Map<String, Map<String, Integer>> getNameToDescToId(Map<Integer, DestInvoke> idToInvoke) {
//        Map<String, Map<String, Integer>> nameToDescToId = new HashMap<>();
//        idToInvoke.forEach(
//                (id, destInvoke) -> nameToDescToId.computeIfAbsent(
//                        destInvoke.getName(),
//                        key -> new HashMap<>()
//                ).put(
//                        destInvoke.getDescriptor(),
//                        id
//                )
//        );
//        return nameToDescToId;
//    }
//
//    private static Integer findInvokeId(MethodNode methodNode, Map<String, Map<String, Integer>> nameToDescToId) {
//        Map<String, Integer> descToId = nameToDescToId.get(methodNode.name);
//        return descToId != null ?
//                descToId.get(methodNode.desc) :
//                null;
//    }
//
//    private static String newInvokeName(Set<String> invokeNames, MethodNode methodNode) {
//        String srcName = methodNode.name + "_@_";
//        while (true) {
//            String destInvokeName = srcName + nameIdGenerator.getAndIncrement();
//            if (!invokeNames.contains(destInvokeName)) {
//                invokeNames.add(destInvokeName);
//                return destInvokeName;
//            }
//        }
//    }
//
//    private static void transformInvoke(ClassNode classNode, MethodNode methodNode, int invokeId, DestInvoke destInvoke, Set<String> invokeNames) {
//        switch (destInvoke.getType()) {
//            case CONSTRUCTOR:
//                transformConstructorInvoke(methodNode, invokeId);
//                break;
//            case METHOD:
//                transformMethodInvoke(
//                        newInvokeName(invokeNames, methodNode),
//                        classNode,
//                        methodNode,
//                        invokeId
//                );
//                break;
//            default:
//                throw new IllegalArgumentException("Unknown dest invoke type: " + destInvoke.getType());
//        }
//    }
//
//    private static void transformConstructorInvoke(MethodNode methodNode, int invokeId) {
//        ListIterator<AbstractInsnNode> iter = methodNode.instructions.iterator();
//        InsnList il = new InsnList();
//        while (iter.hasNext()) {
//            AbstractInsnNode node = iter.next();
//            if (node.getOpcode() == RETURN) {
//                List<LocalVariableNode> args = getArguments(methodNode);
//                addTo(
//                        il,
//                        newInvokeStatic(
//                                getGetInstanceMethod()
//                        ),
//                        newLoadThis(),
//                        newLoadNull(2),
//                        collectArgs(invokeId, args),
//                        newInvokeVirtual(
//                                getOnDelegateMethod()
//                        )
//                );
//                methodNode.instructions.insertBefore(node, il);
//                return;
//            }
//        }
//    }
//
//    private static void transformMethodInvoke(String destInvokeName, ClassNode classNode, MethodNode methodNode, int invokeId) {
//        AsmMethod asmMethod = copyFrom(methodNode);
//        List<LocalVariableNode> args = getArguments(methodNode);
//        asmMethod.add(
//                newInvokeStatic(
//                        getGetInstanceMethod()
//                ),
//                isStatic(methodNode) ?
//                        newLoadNull(1) :
//                        newLoadThis(),
//                newLoadClass("L" + classNode.name + ";"),
//                newLoadLdc(destInvokeName),
//                collectArgs(invokeId, args),
//                newInvokeVirtual(
//                        getOnDelegateMethod()
//                ),
//                createReturn(asmMethod)
//        );
//        classNode.methods.add(
//                asmMethod.getMethodNode()
//        );
//        methodNode.name = destInvokeName;
//    }
//
//    private static Object createReturn(AsmMethod asmMethod) {
//        Object returnNode = newReturn(
//                asmMethod.getReturnType()
//        );
//        if (asmMethod.isVoid())
//            return new Object[]{
//                    returnNode,
//                    newPop()
//            };
//        Object rs = mayCastToReturnType(
//                asmMethod.getReturnType()
//        );
//        return rs == null ?
//                returnNode :
//                new Object[]{
//                        rs,
//                        returnNode
//                };
//    }
//
//    private static Object collectArgs(int invokeId, List<LocalVariableNode> args) {
//        int size = args.size();
//        return new Object[]{
//                newNumLoad(invokeId),
//                newArray(Object.class, size),
//                populateArray(Object.class, args, AsmMethod::newLoadWrapPrimitive)
//        };
//    }
//
//    private static Method getOnDelegateMethod() {
//        return findMethod("onDelegate");
//    }
//
//    private static Method getGetInstanceMethod() {
//        return findMethod("getInstance");
//    }
//
//    private static Method findMethod(String methodName) {
//        return Utils.wrapToRtError(
//                () -> ReflectionUtils.findFirstMethod(ProxyTransformMgr.class, methodName)
//        );
//    }
}
