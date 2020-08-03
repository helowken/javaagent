package test.server.revision;

import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Pair;
import agent.server.transform.revision.ClassDataRepository;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import test.server.AbstractTest;

import java.io.File;
import java.util.Arrays;

import static org.junit.Assert.*;
import static org.objectweb.asm.Opcodes.*;

public class ClassDataRepositoryTest extends AbstractTest {
    @Test
    public void testGetOriginalDataFromResource() throws Exception {
        byte[] data = ClassDataRepository.getInstance().getOriginalClassData(A.class);
        byte[] data2 = IOUtils.readBytes(
                A.class.getResourceAsStream(
                        ClassDataRepository.getClassPathName(A.class)
                )
        );
        assertTrue(Arrays.equals(data, data2));
    }

    @Test
    public void testGetOriginalDataFromOriginalStore() throws Exception {
        Pair<Class<?>, byte[]> p = createClass();
        Class<?> newClass = p.left;
        byte[] data = p.right;

        byte[] data2 = null;
        try {
            data2 = ClassDataRepository.getInstance().getOriginalClassData(newClass);
        } catch (Exception e) {
        }
        assertNull(data2);

        String path = ClassDataRepository.getInstance().getOriginalClassDataPath(newClass);
        FileUtils.mkdirsByFile(path);
        IOUtils.writeBytes(path, data, false);

        data2 = ClassDataRepository.getInstance().getOriginalClassData(newClass);
        assertNotNull(data2);
        assertTrue(Arrays.equals(data, data2));
    }

    @Test
    public void testGetOriginalClassDataFromMemory() throws Exception {
        Pair<Class<?>, byte[]> p = createClass();
        Class<?> newClass = p.left;
        byte[] data = p.right;
        try {
            byte[] data2 = null;
            try {
                data2 = ClassDataRepository.getInstance().getOriginalClassData(newClass);
            } catch (Exception e) {
            }
            assertNull(data2);

            String path = ClassDataRepository.getInstance().getOriginalClassDataPath(newClass);
            assertFalse(new File(path).exists());

            instrumentation.setClassDataFunc(clazz -> data);
            data2 = ClassDataRepository.getInstance().getOriginalClassData(newClass);
            assertNotNull(data2);
            assertTrue(Arrays.equals(data, data2));

            assertTrue(new File(path).exists());
            byte[] data3 = IOUtils.readBytes(path);
            assertTrue(Arrays.equals(data, data3));

            instrumentation.setClassDataFunc(null);
            data2 = ClassDataRepository.getInstance().getOriginalClassData(newClass);
            assertNotNull(data2);
            assertTrue(Arrays.equals(data, data2));

        } finally {
            instrumentation.setClassDataFunc(null);
        }
    }

    @Test
    public void testGetCurrentClassData() throws Exception {
        Pair<Class<?>, byte[]> p = createClass();
        Class<?> newClass = p.left;
        byte[] data = p.right;

        byte[] data2 = null;
        try {
            data2 = ClassDataRepository.getInstance().getCurrentClassData(newClass);
        } catch (Exception e) {
        }
        assertNull(data2);

        ClassDataRepository.getInstance().saveClassData(newClass, data);
        data2 = ClassDataRepository.getInstance().getCurrentClassData(newClass);
        assertNotNull(data2);
        assertTrue(Arrays.equals(data, data2));

        String path = ClassDataRepository.getInstance().getCurrentClassDataPath(newClass);
        assertTrue(new File(path).exists());
        byte[] data3 = IOUtils.readBytes(path);
        assertTrue(Arrays.equals(data, data3));
    }

    private Pair<Class<?>, byte[]> createClass() {
        String className = "test.AA" + System.currentTimeMillis();
        byte[] data = createClassData(className.replace('.', '/'));
        Class<?> newClass = loader.loadClass(className, data);
        return new Pair<>(newClass, data);
    }

    private static byte[] createClassData(String className) {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_5, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE, className, null,
                "java/lang/Object", null);
        cw.visitEnd();
        return cw.toByteArray();
    }

    public static class A {
        public void testAA() {
            System.out.println(111);
        }
    }
}
