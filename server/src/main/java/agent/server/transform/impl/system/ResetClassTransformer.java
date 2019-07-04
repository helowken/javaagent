package agent.server.transform.impl.system;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.server.transform.impl.AbstractTransformer;
import agent.server.transform.impl.TransformerInfo;

import java.io.File;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResetClassTransformer extends AbstractTransformer {
    private static final Logger logger = Logger.getLogger(ResetClassTransformer.class);
    private static final String FILE_SUFFIX = ".class";
    private Map<String, ClassLoader> classNamePathToLoader = new HashMap<>();

    public ResetClassTransformer(Set<Class<?>> classSet) {
        classSet.forEach(clazz -> classNamePathToLoader.put(TransformerInfo.getClassNamePath(clazz), clazz.getClassLoader()));
    }

    @Override
    protected boolean accept(ClassLoader loader, String namePath) {
        return loader.equals(classNamePathToLoader.get(namePath));
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
        byte[] bs = classfileBuffer;
        URL sourceUrl = getSourceURL(protectionDomain, className);
        if (sourceUrl != null) {
            bs = IOUtils.readBytes(sourceUrl.openStream());
            logger.debug("Reset {} success.", targetClassName);
        }
        return bs;
    }

    private URL getSourceURL(ProtectionDomain protectionDomain, String className) {
        try {
            File sourceFile = new File(protectionDomain.getCodeSource().getLocation().getFile(), className + FILE_SUFFIX);
            logger.debug("{} source url: {}", className, sourceFile);
            return sourceFile.toURI().toURL();
        } catch (Exception e) {
            logger.error("Get {} source url failed.", e, className);
        }
        return null;
    }
}
