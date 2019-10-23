package agent.server.transform.impl;

import agent.base.utils.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ResetClassTransformer extends AbstractTransformer {
    private static final Logger logger = Logger.getLogger(ResetClassTransformer.class);
    private final ClassLoader classLoader;
    private final Map<String, ClassLoader> classNamePathToLoader = new HashMap<>();

    public ResetClassTransformer(ClassLoader classLoader, Set<Class<?>> classSet) {
        this.classLoader = classLoader;
        classSet.forEach(clazz -> classNamePathToLoader.put(TransformerInfo.getClassNamePath(clazz), clazz.getClassLoader()));
    }

    @Override
    protected void doTransform(Class<?> clazz) throws Exception {
//
//        URL sourceUrl = classLoader.getResource(className);
//        logger.debug("{} source url: {}", className, sourceUrl);
//        if (sourceUrl != null) {
//            bs = IOUtils.readBytes(sourceUrl.openStream());
//            logger.debug("Reset {} success.", targetClassName);
//        }
    }
}
