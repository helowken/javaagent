package agent.server.transform.revision;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.server.transform.TransformMgr;

import java.io.InputStream;
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
        classToData.putAll(classDataMap);
    }

    public byte[] getClassData(Class<?> clazz) {
        return classToData.computeIfAbsent(clazz, key -> {
            logger.debug("Try to find class data: {}, classLoader: {}", clazz.getName(), clazz.getClassLoader());

            try {
                InputStream inputStream = clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class");
                if (inputStream != null)
                    return IOUtils.readBytes(inputStream);
                else
                    logger.debug("No resource found for: {}", clazz.getName());
            } catch (Exception e) {
                logger.error("Get data from code source failed: {}", e, clazz.getName());
            }

            try {
                classDataTransformer.setTargetClass(clazz);
                TransformMgr.getInstance().reTransformClasses(
                        Collections.singleton(clazz),
                        Collections.singleton(classDataTransformer),
                        (transformClass, error) -> {
                            throw new RuntimeException("Get class data failed: " + clazz.getName(), error);
                        }
                );
                byte[] data = classDataTransformer.getClassData();
                if (data != null) {
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
