package agent.server.transform;

import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.launcher.assist.AssistLauncher;
import agent.server.transform.impl.AbstractTransformer;
import agent.server.transform.impl.TransformerInfo;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClassDataFinder {
    private static final Logger logger = Logger.getLogger(ClassDataFinder.class);
    private static final ClassDataFinder instance = new ClassDataFinder();
    private static final ClassDataTransformer classDataTransformer = new ClassDataTransformer();
    private Map<Class<?>, byte[]> classToData = new HashMap<>();
    private Instrumentation instrumentation;
    private static int id = 0;

    public static ClassDataFinder getInstance() {
        return instance;
    }

    private ClassDataFinder() {
    }

    public void init() {
        instrumentation = AssistLauncher.getInstrumentation();
        if (instrumentation == null)
            logger.error("No assist instrumentation found.");
        else
            instrumentation.addTransformer(classDataTransformer, true);
    }

    public void updateClassData(Class<?> clazz, byte[] data) {
        if (classToData.containsKey(clazz))
            classToData.put(clazz, data);

        Utils.wrapToRtError(() -> {
            String fileName = "/tmp/javaagent/" + TransformerInfo.getClassNamePath(clazz.getName()) + ".class";
            FileUtils.mkdirsByFile(fileName);
            IOUtils.writeBytes(fileName, data, false);
        });
    }

    public byte[] getClassData(Class<?> clazz) {
        byte[] classData = classToData.get(clazz);
        if (classData == null && instrumentation != null) {
            try {
                logger.debug("Get class data for: {}", clazz.getName());
                classDataTransformer.setTargetClass(clazz);
                instrumentation.retransformClasses(clazz);
                classData = classDataTransformer.getClassData();
                logger.debug("Class data for {} is: {}", clazz.getName(), classData);
                classDataTransformer.reset();
                if (classData != null) {
                    classToData.put(clazz, classData);
//
//                    String fileName = "/tmp/javaagent/" + TransformerInfo.getClassNamePath(clazz.getName()) + "_origin_" + (id++) +".class";
//                    FileUtils.mkdirsByFile(fileName);
//                    IOUtils.writeBytes(fileName, classData, false);
                }
            } catch (Throwable e) {
                logger.error("Get class data failed: {}", e, clazz.getName());
            }
        }
        return classData;
    }

    private static class ClassDataTransformer extends AbstractTransformer {
        private Class<?> targetClass;
        private byte[] classData;

        @Override
        protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                     ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
            this.classData = Arrays.copyOf(classfileBuffer, classfileBuffer.length);
            return classfileBuffer;
        }

        @Override
        protected boolean accept(ClassLoader loader, String namePath) {
            return targetClass.getClassLoader() == loader &&
                    targetClass.getName().equals(
                            TransformerInfo.getClassName(namePath)
                    );
        }

        void setTargetClass(Class<?> targetClass) {
            this.targetClass = targetClass;
        }

        byte[] getClassData() {
            return classData;
        }

        void reset() {
            this.targetClass = null;
            this.classData = null;
        }
    }
}
