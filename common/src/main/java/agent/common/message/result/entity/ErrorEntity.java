package agent.common.message.result.entity;

public class ErrorEntity {
    private String className;
    private String transformerKey;
    private String errMsg;

    public ErrorEntity() {
    }

    public ErrorEntity(String className, String errMsg) {
        this(className, null, errMsg);
    }

    public ErrorEntity(String className, String transformerKey, String errMsg) {
        this.className = className;
        this.transformerKey = transformerKey;
        this.errMsg = errMsg;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getTransformerKey() {
        return transformerKey;
    }

    public void setTransformerKey(String transformerKey) {
        this.transformerKey = transformerKey;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }
}
