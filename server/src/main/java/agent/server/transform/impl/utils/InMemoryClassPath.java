package agent.server.transform.impl.utils;

import agent.base.utils.Utils;
import agent.server.transform.impl.TransformerInfo;
import javassist.ClassPath;
import javassist.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

public class InMemoryClassPath implements ClassPath {
    private final Class<?> targetClass;
    private final byte[] classData;

    public InMemoryClassPath(Class<?> targetClass, byte[] classData) {
        this.targetClass = targetClass;
        this.classData = classData;
    }

    @Override
    public InputStream openClassfile(String classname) throws NotFoundException {
        if (targetClass.getName().equals(classname))
            return new ByteArrayInputStream(classData);
        throw new NotFoundException("No class data found by: " + classname);
    }

    @Override
    public URL find(String classname) {
        return targetClass.getName().equals(classname) ?
                Utils.wrapToRtError(
                        () -> new URL("file:///" + TransformerInfo.getClassNamePath(classname))
                ) :
                null;
    }

    @Override
    public void close() {
    }

}
