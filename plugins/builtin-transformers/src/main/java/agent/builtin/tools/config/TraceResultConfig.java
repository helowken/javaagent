package agent.builtin.tools.config;

public class TraceResultConfig extends AbstractResultConfig {
    private int contentSize;
    private boolean displayConsumedTime;
    private boolean displayStartTime;
    private boolean displayArgs;
    private boolean displayRetValue;
    private boolean displayError;
    private int headNum;
    private int tailNum;

    public boolean isDisplayStartTime() {
        return displayStartTime;
    }

    public void setDisplayStartTime(boolean displayStartTime) {
        this.displayStartTime = displayStartTime;
    }

    public int getContentSize() {
        return contentSize;
    }

    public void setContentSize(int contentSize) {
        this.contentSize = contentSize;
    }

    public void setDisplayConsumedTime(boolean displayConsumedTime) {
        this.displayConsumedTime = displayConsumedTime;
    }

    public void setDisplayArgs(boolean displayArgs) {
        this.displayArgs = displayArgs;
    }

    public void setDisplayRetValue(boolean displayRetValue) {
        this.displayRetValue = displayRetValue;
    }

    public void setDisplayError(boolean displayError) {
        this.displayError = displayError;
    }

    public boolean isDisplayConsumedTime() {
        return displayConsumedTime;
    }

    public boolean isDisplayArgs() {
        return displayArgs;
    }

    public boolean isDisplayRetValue() {
        return displayRetValue;
    }

    public boolean isDisplayError() {
        return displayError;
    }

    public int getHeadNum() {
        return headNum;
    }

    public void setHeadNum(int headNum) {
        this.headNum = headNum;
    }

    public int getTailNum() {
        return tailNum;
    }

    public void setTailNum(int tailNum) {
        this.tailNum = tailNum;
    }
}
