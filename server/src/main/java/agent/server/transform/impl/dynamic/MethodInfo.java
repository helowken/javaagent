package agent.server.transform.impl.dynamic;

import agent.server.transform.impl.utils.ClassPathRecorder;

public class MethodInfo extends AbstractMethodInfo {
    public final String className;
    public final String methodName;
    public final String signature;
    public final int level;
    public final boolean isNative;

    public MethodInfo(String className, String methodName, String signature, int level) {
        this.className = className;
        this.methodName = methodName;
        this.signature = signature;
        this.level = level;
        this.isNative = ClassPathRecorder.isNativePackage(className);
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
                level + ");";

    }
}
