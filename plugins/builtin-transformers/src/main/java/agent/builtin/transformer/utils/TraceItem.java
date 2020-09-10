package agent.builtin.transformer.utils;

import agent.base.utils.Utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

public class TraceItem {
    public static final int TYPE_INVOKE = 0;
    public static final int TYPE_CATCH = 1;
    private int id;
    private int parentId;
    private int invokeId;
    private int type;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String costTimeString() {
        NumberFormat df = DecimalFormat.getInstance();
        df.setRoundingMode(RoundingMode.CEILING);
        df.setMaximumFractionDigits(3);
        return df.format(
                ((double) (endTime - startTime)) / (1000 * 1000)
        );
    }
}
