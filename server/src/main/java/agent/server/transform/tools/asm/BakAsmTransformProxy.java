package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static agent.server.transform.tools.asm.AsmMethod.*;

class BakAsmTransformProxy {
//    private static final String METHOD_NAME_SUFFIX = "_@_";
//    private static final AtomicLong nameIdGenerator = new AtomicLong(0);
//
//    static byte[] transform(byte[] classData, Map<Integer, Method> idToMethod) {
//        return AsmUtils.transform(
//                classData,
//                classNode -> doTransform(classNode, idToMethod)
//        );
//    }
//
//    private static void doTransform(ClassNode classNode, Map<Integer, Method> idToMethod) {
//        Map<String, Map<String, Integer>> methodNameToDescToId = newMethodNameToDescToId(idToMethod);
//        List<MethodNode> methodNodes = new ArrayList<>(classNode.methods);
//        Set<String> methodNames = new HashSet<>();
//        methodNodes.forEach(
//                methodNode -> methodNames.add(methodNode.name)
//        );
//        for (MethodNode methodNode : methodNodes) {
//            Integer id = findMethodId(methodNode, methodNameToDescToId);
//            if (id != null)
//                transformMethod(classNode, methodNode, id, methodNames);
//        }
//    }
//
//    private static Map<String, Map<String, Integer>> newMethodNameToDescToId(Map<Integer, Method> idToMethod) {
//        Map<String, Map<String, Integer>> methodNameToDescToId = new HashMap<>();
//        idToMethod.forEach(
//                (id, method) -> methodNameToDescToId.computeIfAbsent(
//                        method.getName(),
//                        key -> new HashMap<>()
//                ).put(
//                        Type.getMethodDescriptor(method),
//                        id
//                )
//        );
//        return methodNameToDescToId;
//    }
//
//    private static Integer findMethodId(MethodNode methodNode, Map<String, Map<String, Integer>> methodNameToDescToId) {
//        Map<String, Integer> descToId = methodNameToDescToId.get(methodNode.name);
//        return descToId != null ?
//                descToId.get(methodNode.desc) :
//                null;
//    }
//
//    private static String newMethodName(Set<String> methodNames, String srcMethodName) {
//        while (true) {
//            String destMethodName = srcMethodName + METHOD_NAME_SUFFIX + nameIdGenerator.getAndIncrement();
//            if (!methodNames.contains(destMethodName)) {
//                methodNames.add(destMethodName);
//                return destMethodName;
//            }
//        }
//    }
//
//    private static void transformMethod(ClassNode classNode, MethodNode methodNode, int methodId, Set<String> methodNames) {
//        String destMethodName = newMethodName(methodNames, methodNode.name);
//        classNode.methods.add(
//                createDelegateMethodNode(destMethodName, classNode, methodNode, methodId)
//        );
//        methodNode.name = destMethodName;
//    }
//
//    private static MethodNode createDelegateMethodNode(String destMethodName, ClassNode classNode, MethodNode methodNode, int methodId) {
//        AsmMethod asmMethod = copyFrom(methodNode);
//        TryCatchBlockNode tryCatchNode = newTryCatch();
//        asmMethod.getMethodNode().tryCatchBlocks.add(tryCatchNode);
//        List<LocalVariableNode> args = getArguments(methodNode);
//        int newLocalVarStartIdx = methodNode.maxLocals;
//        createBeforePart(asmMethod, args, methodId);
//        createCenterPart(asmMethod, destMethodName, classNode, methodNode, tryCatchNode.start);
//        createReturnPart(asmMethod, tryCatchNode.end, args, newLocalVarStartIdx, methodId);
//        createThrowPart(asmMethod, tryCatchNode.handler, args, newLocalVarStartIdx, methodId);
//        return asmMethod.getMethodNode();
//    }
//
//    private static void createBeforePart(AsmMethod asmMethod, List<LocalVariableNode> args, int methodId) {
//        asmMethod.add(
//                newInvokeStatic(
//                        getGetInstanceMethod()
//                ),
//                collectArgs(methodId, args),
//                newInvokeVirtual(
//                        getOnBeforeRunningMethod()
//                )
//        );
//    }
//
//    private static Object collectArgs(int methodId, List<LocalVariableNode> args) {
//        int size = args.size();
//        return new Object[]{
//                newNumLoad(methodId),
//                newArray(Object.class, size),
//                populateArray(Object.class, args, AsmMethod::newLoadWrapPrimitive),
//                newArray(String.class, size),
//                populateArray(
//                        String.class,
//                        args,
//                        AsmMethod::newLoadClassName
//                )
//        };
//    }
//
//    private static void createCenterPart(AsmMethod asmMethod, String destMethodName, ClassNode classNode, MethodNode methodNode, LabelNode labelNode) {
//        asmMethod.add(
//                labelNode,
//                newLoads(methodNode.localVariables),
//                newInvokeVirtual(
//                        classNode.name,
//                        destMethodName,
//                        methodNode.desc
//                )
//        );
//    }
//
//    private static void createReturnPart(AsmMethod asmMethod, LabelNode labelNode, List<LocalVariableNode> args, int startIdx, int methodId) {
//        Type returnType = asmMethod.getReturnType();
//        asmMethod.add(
//                labelNode,
//                newStore(returnType, startIdx),
//                newInvokeStatic(
//                        getGetInstanceMethod()
//                ),
//                collectArgs(methodId, args),
//                newLoad(returnType, startIdx),
//                newLoadClassName(
//                        returnType.getDescriptor()
//                ),
//                newInvokeVirtual(
//                        getOnAfterReturningMethod()
//                ),
//                newLoad(returnType, startIdx),
//                newReturn(
//                        asmMethod.getMethodNode().desc
//                )
//        );
//    }
//
//    private static void createThrowPart(AsmMethod asmMethod, LabelNode labelNode, List<LocalVariableNode> args, int startIdx, int methodId) {
//        Class<?> errClass = Throwable.class;
//        asmMethod.add(
//                labelNode,
//                newStore(errClass, startIdx),
//                newInvokeStatic(
//                        getGetInstanceMethod()
//                ),
//                collectArgs(methodId, args),
//                newLoad(errClass, startIdx),
//                newInvokeVirtual(
//                        getOnAfterThrowingMethod()
//                ),
//                newLoad(errClass, startIdx),
//                newThrow()
//        );
//    }
//
//    private static Method getOnBeforeRunningMethod() {
//        return findMethod("onBeforeRunning");
//    }
//
//    private static Method getOnAfterReturningMethod() {
//        return findMethod("onAfterReturning");
//    }
//
//    private static Method getOnAfterThrowingMethod() {
//        return findMethod("onAfterThrowing");
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
