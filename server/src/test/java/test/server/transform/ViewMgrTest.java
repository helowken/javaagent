package test.server.transform;

import agent.server.transform.impl.ViewMgr;
import agent.server.transform.tools.asm.annotation.OnAfter;
import agent.server.transform.tools.asm.annotation.OnBefore;
import agent.server.transform.tools.asm.annotation.OnReturning;
import agent.server.transform.tools.asm.annotation.OnThrowing;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Collections;
import java.util.Date;

public class ViewMgrTest extends AbstractTest {
    @Test
    public void test() {
        TestProxy proxy = new TestProxy();
        transformByAnnt("testA", Collections.singletonMap(A.class, ".*"), proxy);
        transformByAnnt("testB", Collections.singletonMap(B.class, "<init>"), proxy);
        System.out.println(
                ViewMgr.create(
                        ViewMgr.VIEW_PROXY,
                        null,
                        null,
                        null
                )
        );
    }

    static class A {
        void test() {
        }

        void test2(int a1, boolean a2) {
        }

        String test3(Date date) {
            return "xxx";
        }
    }

    static class B {
        B() {
        }

        B(int a, short b) {
        }

        B(long a, Double b, Float c) {
        }
    }

    static class TestProxy {
        @OnBefore
        void onBefore() {
        }

        @OnAfter
        void onAfter() {
        }

        @OnThrowing
        void onThrowing() {
        }

        @OnReturning
        void onReturning() {
        }
    }
}
