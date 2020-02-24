package agent.builtin.transformer.utils;

import agent.base.utils.Utils;

import java.util.List;
import java.util.Map;

public class TraceItem {
    private int id;
    private int parentId;
    private int invokeId;
    private long startTime;
    private long endTime;
    private List<Map<String, Object>> args;
    private Map<String, Object> returnValue;
    private Map<String, Object> error;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getInvokeId() {
        return invokeId;
    }

    public void setInvokeId(int invokeId) {
        this.invokeId = invokeId;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public List<Map<String, Object>> getArgs() {
        return args;
    }

    public void setArgs(List<Map<String, Object>> args) {
        this.args = args;
    }

    public Map<String, Object> getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Map<String, Object> returnValue) {
        this.returnValue = returnValue;
    }

    public Map<String, Object> getError() {
        return error;
    }

    public void setError(Map<String, Object> error) {
        this.error = error;
    }

    public boolean hasArgs() {
        return !Utils.isEmpty(args);
    }

    public boolean hasReturnValue() {
        return returnValue != null;
    }

    public boolean hasError() {
        return error != null;
    }

    public long costTime() {
        return endTime - startTime;
    }
}
