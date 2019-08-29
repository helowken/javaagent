package test.server.transform.executor;

import agent.server.transform.config.rule.ClassRule;
import agent.server.transform.config.rule.MethodRule;
import agent.server.transform.impl.utils.AgentClassPool;
import javassist.CtClass;
import org.junit.Test;

import static agent.server.transform.config.rule.MethodRule.Position.AFTER;
import static agent.server.transform.config.rule.MethodRule.Position.BEFORE;

public class AnnotationExecutorTest {
    private static final String aClassName = "test.server.transform.config.annotation.TestRule$A";

    @Test
    public void test() {
        CtClass ctClass = AgentClassPool.getInstance().get(aClassName);
//        MethodFinder.getInstance().consume();
    }

    @ClassRule(aClassName)
    private static class TestRule {
        @MethodRule(method = "call", position = BEFORE)
        public void testBefore(Object[] args, Object rv) {
            for (int i = 0; i < args.length; ++i) {
                System.out.println(i + ": " + args[i]);
            }
            System.out.println("rv: " + rv);
        }

        @MethodRule(method = "call", position = AFTER)
        public void testAfter(Object[] args, Object rv) {
            for (int i = 0; i < args.length; ++i) {
                System.out.println(i + ": " + args[i]);
            }
            System.out.println("rv: " + rv);
        }
    }

    private static class A {
        public void call(int a, String b) {
            System.out.println("a = " + a + ", b = " + b);
        }
    }

}
