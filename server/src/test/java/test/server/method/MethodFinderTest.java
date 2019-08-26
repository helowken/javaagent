package test.server.method;

import agent.server.transform.config.MethodFilterConfig;
import agent.server.transform.impl.utils.AgentClassPool;
import agent.server.transform.impl.utils.MethodFinder;
import javassist.CtClass;
import javassist.CtMethod;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class MethodFinderTest {
//    @Test
//    public void testMethodFilter() {
//        MethodFilterConfig config =new MethodFilterConfig();
//        config.setIncludeExprSet(Collections.singleton("load"));
//        CtClass ctClass = AgentClassPool.getInstance().get(LoadTest.class.getName());
//        List<CtMethod> methodList = MethodFinder.getInstance().findByMethodFilter(config, ctClass);
//        methodList.forEach(System.out::println);
//    }
//
//    private static class LoadTest {
//        public static void load(String a) {
//            load(111);
//        }
//
//        public static void load(int n) {
//        }
//    }
}
