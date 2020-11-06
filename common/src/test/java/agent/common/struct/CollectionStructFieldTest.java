package agent.common.struct;

import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructFields;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
public class CollectionStructFieldTest {
    @Test
    public void test() {
        doTest(Arrays.asList(1, 2, 3));
        doTest(Arrays.asList("1", "2", "3"));
        doTest(Arrays.asList(1, 2.4F, 3D, (short) 5, "aaaa", false, 777L, (byte) 8));
        doTest(Arrays.asList(1, 2.4F, 3D, (short) 5, "aaaa", false, 777L, (byte) 8));

        doTest(new HashSet(Arrays.asList(1, 2, 2, 3)));
        doTest(new TreeSet(Arrays.asList("1", "2", "2", "3")));
        doTest(new HashSet(Arrays.asList(1, 2.4F, 3D, 3D, "aaaa", (short) 5, "aaaa", false, 777L, (byte) 8)));
        doTest(new HashSet(Arrays.asList(1, 2.4F, 3D, 3D, "aaaa", (short) 5, "aaaa", false, 777L, (byte) 8)));

        doTest(newList());
        doTest(Collections.singletonList(newMap()));

        doTest(newSet());
        doTest(Collections.singleton(newMap()));
    }

    private Map newMap() {
        Map map = new HashMap();
        map.put("Set", Collections.singleton("s1"));
        map.put("List", Collections.singletonList("l1"));
        map.put("Map", Collections.singletonMap("k1", "v1"));
        map.put("a1", "b1");
        map.put("Null", null);
        return map;
    }

    private Set newSet() {
        Set set = new HashSet();
        set.add(Collections.singleton("s1"));
        set.add(Collections.singletonList("l1"));
        set.add(Collections.singletonMap("k1", "v1"));
        set.add(null);
        return set;
    }

    private List newList() {
        List list = new ArrayList();
        list.add(Collections.singleton("s1"));
        list.add(Collections.singletonList("l1"));
        list.add(Collections.singletonMap("k1", "v1"));
        list.add(null);
        return list;
    }

    private void doTest(Collection value) {
        int len = value.size();
        assertTrue(len > 0);
        ByteBuffer bb = Struct.serialize(value);
        bb.flip();
        Object value2 = Struct.deserialize(bb);
        assertEquals(len, ((Collection) value2).size());
        assertEquals(new ArrayList(value), new ArrayList((Collection) value2));
    }
}
