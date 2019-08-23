package agent.server.transform.impl;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;

import java.net.URL;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResetClassTransformer extends AbstractTransformer {
    private static final Logger logger = Logger.getLogger(ResetClassTransformer.class);
    private static final String FILE_SUFFIX = ".class";
    private final ClassLoader classLoader;
    private final Map<String, ClassLoader> classNamePathToLoader = new HashMap<>();

    public ResetClassTransformer(ClassLoader classLoader, Set<Class<?>> classSet) {
        this.classLoader = classLoader;
        classSet.forEach(clazz -> classNamePathToLoader.put(TransformerInfo.getClassNamePath(clazz), clazz.getClassLoader()));
    }

    @Override
    protected boolean accept(ClassLoader loader, String namePath) {
        return loader.equals(classNamePathToLoader.get(namePath));
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
        byte[] bs = classfileBuffer;
        URL sourceUrl = classLoader.getResource(className);
        logger.debug("{} source url: {}", className, sourceUrl);
        if (sourceUrl != null) {
            bs = IOUtils.readBytes(sourceUrl.openStream());
            logger.debug("Reset {} success.", targetClassName);
        }
        return bs;
    }
}
