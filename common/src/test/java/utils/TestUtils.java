package utils;

import agent.base.utils.IndentUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@SuppressWarnings("unchecked")
public class TestUtils {
    public static void print(Map o) {
        System.out.println(toMapString(o, 1));
    }

    private static String toMapString(Map o, int level) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        o.forEach((k, v) -> {
            String valueString;
            if (v == null)
                valueString = "null";
            else if (v.getClass().isArray())
                valueString = toArrayString(v);
            else if (v instanceof Collection)
                valueString = toCollectionString((Collection) v);
            else if (v instanceof Map)
                valueString = toMapString((Map) v, level + 1);
            else
                valueString = wrap(v);
            sb.append(IndentUtils.getIndent(level)).append(k).append(": \t").append(valueString).append("\n");
        });
        sb.append(IndentUtils.getIndent(level - 1)).append("}");
        return sb.toString();
    }

    private static String toCollectionString(Collection v) {
        StringBuilder sb = new StringBuilder();
        sb.append(v.getClass().getSimpleName()).append("[ ");
        int i = 0;
        for (Object o : v) {
            if (i > 0)
                sb.append(", ");
            sb.append(wrap(o));
            ++i;
        }
        sb.append(" ]");
        return sb.toString();
    }

    private static String toArrayString(Object v) {
        StringBuilder sb = new StringBuilder();
        sb.append("Array[ ");
        for (int i = 0, len = Array.getLength(v); i < len; ++i) {
            if (i > 0)
                sb.append(", ");
            sb.append(wrap(Array.get(v, i)));
        }
        sb.append(" ]");
        return sb.toString();
    }

    private static String wrap(Object v) {
        if (v instanceof String)
            return "\"" + v + "\"";
        return String.valueOf(v);
    }

    public static void checkEquals(Map v1, Map v2) {
        assertNotNull(v1);
        assertNotNull(v2);
        assertEquals(v1.size(), v2.size());
        for (Object key : v1.keySet()) {
            checkEquals(v1.get(key), v2.get(key));
        }
    }

    public static void checkEquals(Object v1, Object v2) {
        if (v1 == null && v2 == null)
            return;
        assertNotNull(v1);
        assertNotNull(v2);
        if (v1.getClass().isArray() && v2.getClass().isArray())
            checkArrayEquals(v1, v2);
        else if (v1 instanceof Map && v2 instanceof Map)
            checkEquals((Map) v1, (Map) v2);
        else
            assertEquals(v1, v2);
    }

    private static void checkArrayEquals(Object v1, Object v2) {
        assertNotNull(v1);
        assertNotNull(v2);
        int len = Array.getLength(v1);
        assertEquals(len, Array.getLength(v2));
        for (int i = 0; i < len; ++i) {
            assertEquals(Array.get(v1, i), Array.get(v2, i));
        }
    }

    public static void main(String[] args) {
        System.out.println(int[].class.getComponentType());
    }
}
