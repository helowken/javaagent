package agent.tools.asm;

import org.objectweb.asm.ClassWriter;

class AsmClassWriter extends ClassWriter {
    private final ClassLoader classLoader;

    AsmClassWriter(int flags, ClassLoader classLoader) {
        super(flags);
        this.classLoader = classLoader;
    }

    @Override
    protected ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    protected String getCommonSuperClass(final String type1, final String type2) {
        ClassLoader classLoader = getClassLoader();
        Class<?> class1;
        try {
            class1 = Class.forName(type1.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            throw new TypeNotPresentException(type1, e);
        }
        Class<?> class2;
        try {
            class2 = Class.forName(type2.replace('/', '.'), false, classLoader);
        } catch (Exception e) {
            throw new TypeNotPresentException(type2, e);
        }
        if (class1.isAssignableFrom(class2)) {
            return type1;
        }
        if (class2.isAssignableFrom(class1)) {
            return type2;
        }
        if (class1.isInterface() || class2.isInterface()) {
            return "java/lang/Object";
        } else {
            do {
                class1 = class1.getSuperclass();
            } while (!class1.isAssignableFrom(class2));
            return class1.getName().replace('.', '/');
        }
    }
}
