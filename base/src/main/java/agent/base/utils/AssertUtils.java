package agent.base.utils;

public class AssertUtils {
    public static void assertNotNull(Object v) {
        assertNotNull(v, "Object is null!");
    }

    public static void assertNotNull(Object v, String msg) {
        if (v == null)
            throw new RuntimeException(msg);
    }

    public static void assertNull(Object v) {
        if (v != null)
            throw new RuntimeException("Object is not null: " + v);
    }

    public static void fail(String msg) {
        throw new RuntimeException(msg);
    }

    public static void assertTrue(boolean v, String msg) {
        if (!v)
            throw new RuntimeException(msg);
    }

    public static void assertFalse(boolean v, String msg) {
        assertTrue(!v, msg);
    }
}
