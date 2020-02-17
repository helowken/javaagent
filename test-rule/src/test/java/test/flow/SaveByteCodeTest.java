package test.flow;

import agent.base.utils.IndentUtils;
import agent.hook.plugin.ClassFinder;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;
import static test.utils.ServerTestUtils.initSystemConfig;
import static test.utils.ServerTestUtils.mockClassFinder;

public class SaveByteCodeTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        initSystemConfig();
        ClassFinder classFinder = mockClassFinder();
        when(classFinder.findClass(anyString(), anyString())).then(new Answer<Class<?>>() {
            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String baseClass = (String) args[1];
                return Thread.currentThread().getContextClassLoader().loadClass(baseClass);
            }
        });
    }

    @Test
    public void test() throws Exception {
        URLClassLoader loader = new URLClassLoader(
                new URL[]{
                        new URL("file:///tmp/javaagent/classes/"),
                        new URL("file:///home/helowken/projects/test-war/target/classes/")
                }
        );
        String className = "test.aop.AAA$$EnhancerBySpringCGLIB$$189be3ec";
        Thread.currentThread().setContextClassLoader(loader);
        Class<?> clazz = loader.loadClass(className);
//        doTest(clazz);
    }

//    private void doTest(Class<?> clazz) {
//        ClassPoolUtils.exec((cp, classPathRecorder) -> {
//            ClassPath classPath = new ClassClassPath(clazz);
//            cp.insertClassPath(classPath);
//            CtClass ctClass = cp.get(clazz.getName());
//            Stream.of(ctClass.getDeclaredMethods())
//                    .map(CtMethod::getLongName)
//                    .forEach(System.out::println);
//            System.out.println("==================");
//            Stream.of(ctClass.getDeclaredFields())
//                    .map(field -> field.getDeclaringClass().getName() + ": " + field.getName())
//                    .forEach(System.out::println);
//            System.out.println("==================");
//            CtMethod ctMethod = ctClass.getDeclaredMethod("testAAA");
//            int level = 0;
//            ctMethod.instrument(new TestExprEditor(level));
//        });
//    }

    private final static Set<String> traversedMethodSet = new HashSet<>();

    private static class TestExprEditor extends ExprEditor {
        private final int level;

        private TestExprEditor(int level) {
            this.level = level;
        }

        @Override
        public void edit(MethodCall mc) {
            try {
                CtMethod childMethod = mc.getMethod();
                String methodLongName = childMethod.getLongName();
                if (!traversedMethodSet.contains(methodLongName)) {
                    traversedMethodSet.add(methodLongName);
//                    if (!ReflectionUtils.isJavaNativePackage(childMethod.getDeclaringClass().getName())) {
                        System.out.println(IndentUtils.getIndent(level) + methodLongName);
                        if (!childMethod.isEmpty()) {
                            System.out.println("instrument: " + methodLongName);
                            childMethod.instrument(new TestExprEditor(level + 1));
                        } else
                            System.out.println("==========>>> empty: " + methodLongName);
                        if (Modifier.isAbstract(childMethod.getModifiers())) {
                            String baseClassName = childMethod.getDeclaringClass().getName();
//                            ClassCache.getInstance().getSubClassMap(baseClassName).forEach((key, value) -> System.out.println("key: " + key));
                        }
//                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
