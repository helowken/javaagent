package agent.builtin.tools.result.data;

import agent.builtin.tools.result.CostTimeStatItem;
import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;

import static agent.builtin.tools.result.data.CallChainData.POJO_TYPE;

@PojoClass(type = POJO_TYPE)
public class CallChainData {
    public static final int POJO_TYPE = 1;
    @PojoProperty(index = 1)
    public int id;
    @PojoProperty(index = 2)
    public int invokeId;
    @PojoProperty(index = 3)
    public CostTimeStatItem item;

    public CallChainData() {
    }

    public CallChainData(int id, int invokeId, CostTimeStatItem item) {
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

    public CostTimeStatItem getItem() {
        return item;
    }

    public void setItem(CostTimeStatItem item) {
        this.item = item;
    }
}
