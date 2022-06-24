package agent.builtin.tools.result.data;

import agent.base.struct.annotation.PojoClass;
import agent.base.struct.annotation.PojoProperty;

import static agent.builtin.tools.result.data.CallChainData.POJO_TYPE;

@PojoClass(type = POJO_TYPE)
public class CallChainData {
    public static final int POJO_TYPE = 1;
    @PojoProperty(index = 1)
    public int id;
    @PojoProperty(index = 2)
    public int invokeId;
    @PojoProperty(index = 3)
    public ConsumedTimeStatItem item;

    public CallChainData() {
    }

    public CallChainData(int id, int invokeId, ConsumedTimeStatItem item) {
        this.id = id;
        this.invokeId = invokeId;
        this.item = item;
    }

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

    public ConsumedTimeStatItem getItem() {
        return item;
    }

    public void setItem(ConsumedTimeStatItem item) {
        this.item = item;
    }
}
