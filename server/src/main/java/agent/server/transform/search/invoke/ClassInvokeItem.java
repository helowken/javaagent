package agent.server.transform.search.invoke;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassInvokeItem {
    private final Map<String, List<InnerInvokeItem>> invokeToInnerInvokes = new HashMap<>();

    public static String newInvokeKey(String name, String desc) {
        return name + desc;
    }

    void add(String invokeName, String invokeDesc, List<InnerInvokeItem> innerInvokeItems) {
        invokeToInnerInvokes.put(
                newInvokeKey(invokeName, invokeDesc),
                innerInvokeItems
        );
    }

    public List<InnerInvokeItem> getAndRemove(String invokeKey) {
        return invokeToInnerInvokes.remove(invokeKey);
    }
}
