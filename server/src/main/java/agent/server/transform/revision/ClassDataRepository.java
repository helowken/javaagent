package agent.server.transform.revision;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.TimeMeasureUtils;
import agent.server.transform.InstrumentationMgr;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClassDataRepository {
    private static final Logger logger = Logger.getLogger(ClassDataRepository.class);
    private static final ClassDataRepository instance = new ClassDataRepository();
    private Map<Class<?>, byte[]> classToData = new ConcurrentHashMap<>();

    public static ClassDataRepository getInstance() {
        return instance;
    }

    private ClassDataRepository() {
    }

    public void saveClassData(Map<Class<?>, byte[]> classDataMap) {
        classToData.putAll(classDataMap);
    }

    private byte[] loadClassDataFromResource(Class<?> clazz) {
        return TimeMeasureUtils.run(
                () -> {
                    InputStream inputStream = clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class");
                    if (inputStream != null)
                        return IOUtils.readBytes(inputStream);
                    logger.debug("No resource found for: {}", clazz.getName());
                    return null;
                },
                e -> {
                    logger.error("Get data from code source failed: {}", e, clazz.getName());
                    return null;
                },
                "loadClassDataFromResource: {} , {}",
                clazz.getName()
        );
    }

    private byte[] getClassDataFromInstrumentation(Class<?> clazz) {
        return TimeMeasureUtils.run(
                () -> {
                    logger.debug("Get class data from memory: {}", clazz.getName());
                    GetClassDataTransformer transformer = new GetClassDataTransformer(clazz);
                    InstrumentationMgr.getInstance().retransform(transformer, clazz);
                    byte[] data = transformer.getData();
                    if (data != null) {
                        try {
                            ClassDataStore.save(clazz, data, ClassDataStore.REVISION_0);
                        } catch (Throwable t) {
                            logger.error("save class data failed: {}", t, clazz.getName());
                            return null;
                        }
                    }
                    return data;
                },
                t -> {
                    logger.error("Get class data failed.", t);
                    return null;
                },
                "getClassDataFromInstrumentation: {} , {}",
                clazz.getName()
        );
    }

    public byte[] getClassData(Class<?> clazz) {
        return classToData.computeIfAbsent(
                clazz,
                key -> {
                    logger.debug("Try to find class data: {}, classLoader: {}", clazz.getName(), clazz.getClassLoader());
                    byte[] data = loadClassDataFromResource(clazz);
                    if (data == null)
                        data = getClassDataFromInstrumentation(clazz);
                    if (data != null)
                        return data;
                    throw new RuntimeException("No data found for class: " + clazz);
                }
        );
    }

}
