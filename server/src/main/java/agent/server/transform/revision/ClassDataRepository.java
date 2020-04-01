package agent.server.transform.revision;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
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
        long st = System.currentTimeMillis();
        try {
            InputStream inputStream = clazz.getResourceAsStream("/" + clazz.getName().replace('.', '/') + ".class");
            if (inputStream != null)
                return IOUtils.readBytes(inputStream);
            else
                logger.debug("No resource found for: {}", clazz.getName());
        } catch (Exception e) {
            logger.error("Get data from code source failed: {}", e, clazz.getName());
        } finally {
            long et = System.currentTimeMillis();
            logger.debug("loadClassDataFromResource: {} , {}", (et - st), clazz.getName());
        }
        return null;
    }

    private byte[] getClassDataFromInstrumentation(Class<?> clazz) {
        long st = System.currentTimeMillis();
        try {
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
        } catch (Throwable t) {
            logger.error("Get class data failed.", t);
            return null;
        } finally {
            long et = System.currentTimeMillis();
            logger.debug("getClassDataFromInstrumentation: {} , {}", (et - st), clazz.getName());
        }
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
