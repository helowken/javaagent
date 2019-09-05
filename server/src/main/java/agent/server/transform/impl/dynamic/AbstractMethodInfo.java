package agent.server.transform.impl.dynamic;

abstract class AbstractMethodInfo {
    public final String className;
    public final String methodName;
    public final String signature;

    AbstractMethodInfo(String className, String methodName, String signature) {
        this.className = className;
        this.methodName = methodName;
        this.signature = signature;
    }

    @Override
    public String toString() {
        return className + "#" + methodName + signature;
    }
}
