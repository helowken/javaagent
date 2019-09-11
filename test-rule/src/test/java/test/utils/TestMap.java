package test.utils;

import java.util.HashMap;

public class TestMap extends HashMap {
    public Object put(Object k, Object v) {
        return super.put(k, v);
    }
}
