package agent.common.struct;

import org.junit.Test;
import agent.common.buffer.BufferAllocator;
import agent.common.struct.StructField;
import agent.common.struct.impl.StructFields;
import utils.TestUtils;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@SuppressWarnings("unchecked")
public class MapStructFieldTest {
    @Test
    public void test() {
        Map map = newMap("");
        map.put("Map", newMap("sub-"));
        StructField field = StructFields.newMap();
        ByteBuffer bb = BufferAllocator.allocate(field.bytesSize(map));
        field.serialize(bb, map);
        bb.flip();
        Map<String, Object> map2 = (Map) field.deserialize(bb);
        TestUtils.checkEquals(map, map2);
        TestUtils.print(map);
    }

    private Map newMap(String keyPrefix) {
        Map<String, Object> map = new HashMap<>();
        map.put(keyPrefix + "Null", null);
        map.put(keyPrefix + "byte", (byte) 1);
        map.put(keyPrefix + "short", (short) 2);
        map.put(keyPrefix + "int", 3);
        map.put(keyPrefix + "long", 4L);
        map.put(keyPrefix + "float", 5F);
        map.put(keyPrefix + "double", 6);
        map.put(keyPrefix + "String", "7777");

        map.put(keyPrefix + "byte[]", new byte[]{1, 2, 3});
        map.put(keyPrefix + "Byte[]", new Byte[]{1, 2, 3});
        map.put(keyPrefix + "boolean[]", new boolean[]{true, false});
        map.put(keyPrefix + "Boolean[]", new Boolean[]{true, false});
        map.put(keyPrefix + "short[]", new short[]{1, 2, 3});
        map.put(keyPrefix + "Short[]", new Short[]{1, 2, 3});
        map.put(keyPrefix + "int[]", new int[]{1, 2, 3});
        map.put(keyPrefix + "Integer[]", new Integer[]{1, 2, 3});
        map.put(keyPrefix + "long[]", new long[]{1, 2, 3});
        map.put(keyPrefix + "Long[]", new Long[]{1L, 2L, 3L});
        map.put(keyPrefix + "float[]", new float[]{1, 2, 3});
        map.put(keyPrefix + "Float[]", new Float[]{1F, 2F, 3F});
        map.put(keyPrefix + "double[]", new double[]{1, 2, 3});
        map.put(keyPrefix + "Double[]", new Double[]{1D, 2D, 3D});
        map.put(keyPrefix + "String[]", new String[]{"aaa", "bbb", "ccc"});

        map.put(keyPrefix + "List", Arrays.asList(1, "2", 3.3D));
        map.put(keyPrefix + "Set", new HashSet(Arrays.asList(1, "2", 3.3D)));
        return map;
    }
}
