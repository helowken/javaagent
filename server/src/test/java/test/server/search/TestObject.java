package test.server.search;

public class TestObject {
    public void test() {
        testLambda(
                (s, err) -> System.out.println(s + ": " + err.getMessage())
        );
    }

    private void testLambda(TestFunc func) {
        func.handle("aaa", new Exception("bbb"));
    }

    public interface TestFunc {
        void handle(String s, Exception e);
    }
}
