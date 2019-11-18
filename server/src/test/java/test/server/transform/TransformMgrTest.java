package test.server.transform;

import agent.server.transform.AgentTransformer;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformMgr;
import agent.server.transform.TransformResult;
import agent.server.transform.impl.AbstractTransformer;
import agent.server.transform.revision.ClassDataRepository;
import javassist.CtClass;
import javassist.CtField;
import org.junit.Test;
import test.server.AbstractServerTest;
import test.server.utils.TestClassLoader;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;

import static agent.server.transform.TransformContext.ACTION_MODIFY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TransformMgrTest extends AbstractServerTest {
    @Test
    public void testTransform() throws Exception {
        TestClassLoader testClassLoader = new TestClassLoader(
                new URL[]{
                        getClass().getProtectionDomain().getCodeSource().getLocation()
                }
        );
        classFinder.set(defaultContext, testClassLoader);

        Class<?> clazz = testClassLoader.loadClass(A.class.getName());
        System.out.println(clazz.getClassLoader());
        Class<?> oldClass = loadClassFromRepository(clazz);
        assertEquals(0, oldClass.getDeclaredFields().length);

        final String fieldName1 = "a";
        doTransform(clazz, new ATransformer(fieldName1));

        Class<?> newClass = loadClassFromRepository(clazz);
        assertEquals(1, newClass.getDeclaredFields().length);
        checkField(newClass, fieldName1, int.class);

        final String fieldName2 = "b";
        doTransform(clazz, new ATransformer(fieldName2));

        Class<?> newClass2 = loadClassFromRepository(clazz);
        assertEquals(2, newClass2.getDeclaredFields().length);
        checkField(newClass2, fieldName2, int.class);
    }

    private void checkField(Class<?> clazz, String name, Class<?> type) throws Exception {
        Field field = clazz.getDeclaredField(name);
        assertEquals(type, field.getType());
    }

    private Class<?> loadClassFromRepository(Class<?> clazz) {
        byte[] data = ClassDataRepository.getInstance().getClassData(clazz);
        return new TestClassLoader().loadClass(clazz.getName(), data);
    }

    private TransformResult doTransform(Class<?> clazz, AgentTransformer transformer) {
        TransformContext transformContext = new TransformContext(
                defaultContext,
                clazz,
                transformer,
                ACTION_MODIFY
        );
        TransformResult result = TransformMgr.getInstance().transform(
                Collections.singletonList(transformContext)
        ).get(0);
        assertFalse(result.hasError());
        return result;
    }

    private static class A {
    }

    private static class ATransformer extends AbstractTransformer {
        private final String fieldName;

        private ATransformer(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        protected void doTransform(Class<?> clazz) throws Exception {
            CtClass ctClass = getClassPool().get(clazz.getName());
            ctClass.addField(new CtField(CtClass.intType, fieldName, ctClass));
        }
    }
}
