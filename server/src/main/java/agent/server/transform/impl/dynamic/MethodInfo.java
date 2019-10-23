package agent.server.transform.impl.dynamic;

import agent.server.transform.cp.AgentClassPool;

public class MethodInfo extends AbstractMethodInfo {
    public final String className;
    public final String methodName;
    public final String signature;
    public final int classModifiers;
    public final int methodModifiers;
    public final int level;
    public final boolean isNativePackage;

    public MethodInfo(String className, String methodName, String signature, int classModifiers, int methodModifiers, int level) {
        this.className = className;
        this.methodName = methodName;
        this.signature = signature;
        this.classModifiers = classModifiers;
        this.methodModifiers = methodModifiers;
        this.level = level;
        this.isNativePackage = AgentClassPool.isNativePackage(className);
    }

    @Override
    public String toString() {
        return className + "#" + methodName + signature;
    }

    public String newCode() {
        return "new " + getClass().getName() + "(\"" +
                className + "\", \"" +
                methodName + "\", \"" +
                signature + "\", " +
                classModifiers + ", " +
                methodModifiers + ", " +
                level + ");";

    }
}
