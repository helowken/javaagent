package agent.server.transform;

import agent.base.utils.Logger;
import agent.launcher.assist.AssistLauncher;
import agent.server.transform.impl.AbstractTransformer;
import agent.server.transform.impl.TransformerInfo;

import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.Arrays;

public class ClassDataFinder {
    private static final Logger logger = Logger.getLogger(ClassDataFinder.class);
    private static final ClassDataFinder instance = new ClassDataFinder();
    private static final ClassDataTransformer classDataTransformer = new ClassDataTransformer();
    private Instrumentation instrumentation;

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

    public byte[] getClassData(Class<?> clazz) {
        if (instrumentation != null) {
            try {
                classDataTransformer.setTargetClass(clazz);
                instrumentation.retransformClasses(clazz);
                byte[] classData = classDataTransformer.getClassData();
                logger.debug("Class data for {} is: {}", clazz.getName(), classData);
                classDataTransformer.reset();
                return classData;
            } catch (Exception e) {
                logger.error("Get class data failed.", e);
            }
        }
        return null;
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
