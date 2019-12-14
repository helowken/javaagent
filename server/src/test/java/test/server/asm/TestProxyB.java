package test.server.asm;

import agent.server.transform.tools.asm.ProxyCallChain;

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

    public void testAfter(Object returnValue, Class<?> returnType, Throwable error) {
        this.logList.add("after-" + count);
        System.out.println(
                "==== test after: " + count +
                        (error != null ?
                                ", Error: " + error :
                                ", Return Value: " + returnValue + ", Return Type: " + returnType
                        )
        );
    }

    public void testAround(ProxyCallChain callChain) {
        this.logList.add("aroundStart-" + count);
        System.out.println("==== test around start: " + count);
        callChain.process();
        this.logList.add("aroundEnd-" + count);
        System.out.println("==== test around end: " + count);
    }

    public void testAfterReturning(Object returnValue, Class<?> returnType) {
        this.logList.add("afterReturning-" + count);
        System.out.println(
                "==== test after returning: " + count +
                        ", Return Value: " + returnValue +
                        ", Return Type: " + returnType
        );
    }

    public void testAfterThrowing(Throwable error) {
        this.logList.add("afterThrowing-" + count);
        System.out.println(
                "==== test after throwing: " + count +
                        ", Error: " + error
        );
    }
}
