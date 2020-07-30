package agent.server.transform.search.invoke;

import static agent.server.transform.search.invoke.ClassInvokeItem.newInvokeKey;

public class InnerInvokeItem {
    private final String owner;
    private final String name;
    private final String desc;
    private final boolean dynamic;

    InnerInvokeItem(String owner, String name, String desc, boolean dynamic) {
        this.owner = owner;
        this.name = name;
        this.desc = desc;
        this.dynamic = dynamic;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public String getInvokeKey() {
        return newInvokeKey(name, desc);
    }

    @Override
    public String toString() {
        return "InvokeItem{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", owner='" + owner + '\'' +
                ", dynamic=" + dynamic +
                '}';
    }
}
