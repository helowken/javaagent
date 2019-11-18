package agent.server.transform.cp;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.Logger;
import agent.base.utils.Utils;
import agent.server.transform.TransformMgr;
import agent.server.transform.revision.ClassDataRepository;
import javassist.ClassPath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

public class InMemoryClassPath implements ClassPath {
    private static final Logger logger = Logger.getLogger(InMemoryClassPath.class);
    private final String context;

    InMemoryClassPath(String context) {
        this.context = context;
    }

    @Override
    public InputStream openClassfile(String className) {
        byte[] data = ClassDataRepository.getInstance().getClassData(
                findClass(className)
        );
        logger.debug("Get context {} class {} data: {}", context, className, data);
        return new ByteArrayInputStream(data);
    }

    private Class<?> findClass(String className) {
        return TransformMgr.getInstance().getClassFinder().findClass(context, className);
    }

    @Override
    public URL find(String className) {
        try {
            Class<?> clazz = findClass(className);
            if (!ClassLoaderUtils.isSystem(clazz.getClassLoader()))
                return Utils.wrapToRtError(
                        () -> new URL("http://fake/" + className)
                );
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public void close() {
    }

}
