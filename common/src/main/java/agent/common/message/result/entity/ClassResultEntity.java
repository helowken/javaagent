package agent.common.message.result.entity;

import java.util.ArrayList;
import java.util.List;

public class ClassResultEntity {
    private String className;
    private List<String> invokeList = new ArrayList<>();

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<String> getInvokeList() {
        return invokeList;
    }

    public void setInvokeList(List<String> invokeList) {
        this.invokeList = invokeList;
    }

    public void addInvoke(String invoke) {
        this.invokeList.add(invoke);
    }

}
