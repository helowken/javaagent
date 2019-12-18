package test.server.asm;

import java.util.Arrays;
import java.util.List;

public class TestProxyB {
    private final int count;
    private final List<String> logList;

    TestProxyB(int count, List<String> logList) {
        this.count = count;
        this.logList = logList;
    }

    public void testBefore(Object[] args, Class<?>[] argTypes) {
        this.logList.add("before-" + count);
        System.out.println(
                "==== test before: " + count +
                        ", Args: " + Arrays.toString(args) +
                        ", Arg Types: " + Arrays.toString(argTypes)
        );
    }

    public void testOnReturning(Object returnValue, Class<?> returnType) {
        this.logList.add("onReturning-" + count);
        System.out.println(
                "==== test on returning: " + count +
                        ", Return Value: " + returnValue +
                        ", Return Type: " + returnType
        );
    }

    public void testOnThrowing(Throwable error) {
        this.logList.add("onThrowing-" + count);
        System.out.println(
                "==== test on throwing: " + count +
                        ", Error: " + error
        );
    }

    public void testAfter() {
        this.logList.add("after-" + count);
        System.out.println("==== test after: " + count);
    }
}
