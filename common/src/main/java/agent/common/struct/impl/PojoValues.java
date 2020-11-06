package agent.common.struct.impl;

import java.util.ArrayList;
import java.util.Collection;

class PojoValues extends ArrayList<Object> {
    final int type;

    PojoValues(int type, Collection<Object> values) {
        super(values);
        this.type = type;
    }
}
