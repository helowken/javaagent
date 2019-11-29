package agent.delegate;

import agent.base.utils.TypeObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JSONDelegateTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testReadMap() throws Exception {
        Map<String, Object> map = newMap();
        Map<String, Object> map2 = JSONDelegate.read(
                getContent(map)
        );
        assertEquals(map, map2);
    }

    @Test
    public void testReadObject() throws Exception {
        Map<String, Object> map = newMap();
        A a = JSONDelegate.read(
                getContent(map),
                new TypeObject<A>() {
                }.type
        );
        checkA(map, a);
    }

    @Test
    public void testReadObjectList() throws Exception {
        Map<String, Object> map = newMap();
        List<Map<String, Object>> mapList = new ArrayList<>();
        final int size = 3;
        for (int i = 0; i < size; ++i) {
            mapList.add(map);
        }
        List<A> aList = JSONDelegate.read(
                getContent(mapList),
                new TypeObject<List<A>>() {
                }.type
        );
        checkAList(map, size, aList);
    }

    @Test
    public void testConvertObject() {
        Map<String, Object> map = newMap();
        A a = JSONDelegate.convert(map,
                new TypeObject<A>() {
                }.type
        );
        checkA(map, a);

        Map<String, Object> map2 = JSONDelegate.convert(a,
                new TypeObject<Map>() {
                }.type
        );
        checkA(map2, a);
    }

    @Test
    public void testConvertObjectList() {
        Map<String, Object> map = newMap();
        final int size = 3;
        List<Map<String, Object>> mapList = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            mapList.add(map);
        }
        List<A> aList = JSONDelegate.convert(mapList,
                new TypeObject<List<A>>() {
                }.type
        );
        checkAList(map, size, aList);

        List<Map<String, Object>> mapList2 = JSONDelegate.convert(aList,
                new TypeObject<List<Map>>() {
                }.type
        );
        assertEquals(mapList, mapList2);
    }

    private Map<String, Object> newMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("a", 11);
        map.put("b", "xxx");
        return map;
    }

    private String getContent(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private void checkA(Map<String, Object> map, A a) {
        assertEquals(map.get("a"), a.getA());
        assertEquals(map.get("b"), a.getB());
    }

    private void checkAList(Map<String, Object> map, final int size, List<A> aList) {
        assertEquals(size, aList.size());
        aList.forEach(a -> checkA(map, a));
    }

    static class A {
        private int a;
        private String b;

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }
    }

}
