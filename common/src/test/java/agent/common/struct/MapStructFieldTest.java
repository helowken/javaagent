package agent.common.struct;

import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructFields;
import org.junit.Test;
import utils.TestUtils;

import java.nio.ByteBuffer;
import java.util.*;

@SuppressWarnings("unchecked")
public class MapStructFieldTest {
    @Test
    public void test() {
        Map map = newMap("");
        map.put("Map", newMap("sub-"));
        checkEquals(map);
    }

    private void checkEquals(Map<String, Object> map) {
        ByteBuffer bb = Struct.serialize(map);
        bb.flip();
        Map<String, Object> map2 = Struct.deserialize(bb);
        TestUtils.checkEquals(map, map2);
        TestUtils.print(map);
        System.out.println("============");
        TestUtils.print(map2);
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

    @Test
    public void testTreeMap() {
        Map<String, Object> map = new TreeMap<>();
        Map<String, Object> cm = new TreeMap<>();
        cm.put("testAfterReturn()", "void");
        cm.put("doBefore2()", "void");
        cm.put("testBefore2()", "void");
        cm.put("testAfter()", "void");
        cm.put("testAfter2()", "void");
        cm.put("doAfterReturn()", "void");
        cm.put("doAfter()", "void");
        cm.put("doBefore()", "void");
        cm.put("testPointcut()", "void");
        cm.put("doAfter2()", "void");
        cm.put("testBefore()", "void");
        map.put("xxxx", cm);
        checkEquals(map);
    }
}
