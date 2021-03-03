package agent.common.message.result.entity;

import agent.base.struct.annotation.PojoProperty;

public class ErrorEntity {
    @PojoProperty(index = 0)
    private String className;
    @PojoProperty(index = 1)
    private String transformerKey;
    @PojoProperty(index = 2)
    private String errMsg;

    public ErrorEntity() {
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

    @Override
    public String toString() {
        return "ErrorEntity{" +
                "className='" + className + '\'' +
                ", transformerKey='" + transformerKey + '\'' +
                ", errMsg='" + errMsg + '\'' +
                '}';
    }
}
