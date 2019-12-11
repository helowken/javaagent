package agent.server.transform.tools.asm;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static agent.server.transform.tools.asm.AsmMethod.*;

class NewAsmTransformProxy {
    private static final String METHOD_NAME_SUFFIX = "_@_";
    private static final AtomicLong nameIdGenerator = new AtomicLong(0);

    static byte[] transform(byte[] classData, Map<Integer, Method> idToMethod) {
        return AsmUtils.transform(
                classData,
                classNode -> doTransform(classNode, idToMethod)
        );
    }

    private static void doTransform(ClassNode classNode, Map<Integer, Method> idToMethod) {
        Map<String, Map<String, Integer>> methodNameToDescToId = newMethodNameToDescToId(idToMethod);
        List<MethodNode> methodNodes = new ArrayList<>(classNode.methods);
        Set<String> methodNames = new HashSet<>();
        methodNodes.forEach(
                methodNode -> methodNames.add(methodNode.name)
        );
        for (MethodNode methodNode : methodNodes) {
            Integer id = findMethodId(methodNode, methodNameToDescToId);
            if (id != null)
                transformMethod(classNode, methodNode, id, methodNames);
        }
    }

    private static Map<String, Map<String, Integer>> newMethodNameToDescToId(Map<Integer, Method> idToMethod) {
        Map<String, Map<String, Integer>> methodNameToDescToId = new HashMap<>();
        idToMethod.forEach(
                (id, method) -> methodNameToDescToId.computeIfAbsent(
                        method.getName(),
                        key -> new HashMap<>()
                ).put(
                        Type.getMethodDescriptor(method),
                        id
                )
        );
        return methodNameToDescToId;
    }

    private static Integer findMethodId(MethodNode methodNode, Map<String, Map<String, Integer>> methodNameToDescToId) {
        Map<String, Integer> descToId = methodNameToDescToId.get(methodNode.name);
        return descToId != null ?
                descToId.get(methodNode.desc) :
                null;
    }

    private static String newMethodName(Set<String> methodNames, String srcMethodName) {
        while (true) {
            String destMethodName = srcMethodName + METHOD_NAME_SUFFIX + nameIdGenerator.getAndIncrement();
            if (!methodNames.contains(destMethodName)) {
                methodNames.add(destMethodName);
                return destMethodName;
            }
        }
    }

    private static void transformMethod(ClassNode classNode, MethodNode methodNode, int methodId, Set<String> methodNames) {
        String destMethodName = newMethodName(methodNames, methodNode.name);
        classNode.methods.add(
                createDelegateMethodNode(destMethodName, classNode, methodNode, methodId)
        );
        methodNode.name = destMethodName;
    }

    private static MethodNode createDelegateMethodNode(String destMethodName, ClassNode classNode, MethodNode methodNode, int methodId) {
        AsmMethod asmMethod = copyFrom(methodNode);
        List<LocalVariableNode> args = getArguments(methodNode);
        asmMethod.add(
                newInvokeStatic(
                        getGetInstanceMethod()
                ),
                isStatic(methodNode) ?
                        newLoadNull(1) :
                        newLoadThis(),
                newLoadClass("L" + classNode.name + ";"),
                newLoadLdc(destMethodName),
                collectArgs(methodId, args),
                newInvokeVirtual(
                        getOnDelegateMethod()
                ),
                createReturn(asmMethod)
        );
        return asmMethod.getMethodNode();
    }

    private static Object createReturn(AsmMethod asmMethod) {
        if (asmMethod.isVoid())
            return newPop();
        Object rs = mayCastToReturnType(
                asmMethod.getReturnType()
        );
        Object returnNode = newReturn(
                asmMethod.getReturnType()
        );
        return rs == null ?
                returnNode :
                new Object[]{
                        rs,
                        returnNode
                };
    }

    private static Object collectArgs(int methodId, List<LocalVariableNode> args) {
        int size = args.size();
        return new Object[]{
                newNumLoad(methodId),
                newArray(Object.class, size),
                populateArray(Object.class, args, AsmMethod::newLoadWrapPrimitive)
        };
    }

    private static Method getOnDelegateMethod() {
        return findMethod("onDelegate");
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
