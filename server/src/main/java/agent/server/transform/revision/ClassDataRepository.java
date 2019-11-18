package agent.server.transform.revision;

import agent.base.utils.Logger;
import agent.server.transform.TransformMgr;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassDataRepository {
    private static final Logger logger = Logger.getLogger(ClassDataRepository.class);
    private static final ClassDataRepository instance = new ClassDataRepository();
    private static final GetClassDataTransformer classDataTransformer = new GetClassDataTransformer();
    private Map<Class<?>, byte[]> classToData = new ConcurrentHashMap<>();

    public static ClassDataRepository getInstance() {
        return instance;
    }

    private ClassDataRepository() {
    }

    public void saveClassData(Map<Class<?>, byte[]> classDataMap) {
        logger.debug("Save class data: {}", classDataMap);
        classToData.putAll(classDataMap);
    }

    public byte[] getClassData(Class<?> clazz) {
        return classToData.computeIfAbsent(clazz, key -> {
            try {
                logger.debug("Try to find class data: {}, classLoader: {}", clazz.getName(), clazz.getClassLoader());
                classDataTransformer.setTargetClass(clazz);
                TransformMgr.getInstance().reTransformClasses(
                        Collections.singleton(clazz),
                        Collections.singleton(classDataTransformer),
                        (transformClass, error) -> {
                            throw new RuntimeException(error);
                        }
                );
                byte[] data = classDataTransformer.getClassData();
                if (data != null) {
                    logger.debug("Found data for class: {}, classLoader: {}", clazz.getName(), clazz.getClassLoader());
                    ClassDataStore.save(clazz, data, ClassDataStore.REVISION_0);
                    return data;
                }
                throw new RuntimeException("No data found for class: " + clazz);
            } finally {
                classDataTransformer.reset();
            }
        });
    }

}
