package test.server.transform;

import agent.server.transform.impl.utils.AgentClassPool;
import javassist.CtClass;
import org.junit.Test;

import java.util.stream.Stream;

public class TransformTest {
    @Test
    public void test2() {
        CtClass ctClass = AgentClassPool.getInstance().get("java.util.ArrayList");
        Stream.of(ctClass.getConstructors())
                .forEach(c -> System.out.println(c.getSignature()));
    }
}
