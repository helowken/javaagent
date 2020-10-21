package agent.common.struct.impl;

import agent.common.struct.BBuff;
import agent.common.struct.Struct;
import agent.common.struct.StructField;

import java.util.List;

@SuppressWarnings("unchecked")
public class PojoStruct implements Struct {
    private final StructField field = StructFields.newPojo();
    private Object pojo;

    public Object getPojo() {
        return pojo;
    }

    public void setPojo(Object pojo) {
        this.pojo = pojo;
    }

    @Override
    public void deserialize(BBuff bb) {
        if (pojo == null)
            return;
        List<Object> values = (List) field.deserialize(bb);
        PojoStructCache.setPojoValues(pojo, values);
    }

    @Override
    public void serialize(BBuff bb) {
        field.serialize(bb, pojo);
    }

    @Override
    public int bytesSize() {
        return field.bytesSize(pojo);
    }


}
