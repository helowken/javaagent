package agent.server.transform.revision;

import agent.base.utils.*;
import agent.server.transform.InstrumentationMgr;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;

public class ClassDataRepository {
    private static final Logger logger = Logger.getLogger(ClassDataRepository.class);
    private static final String KEY_CLASS_DATA_BASE_DIR = "class.data.base.dir";
    private static final String TMP_DIR = "agent_class_data";
    private static final String ORIGINAL_DIR = "original";
    private static final String CURR_DIR = "current";
    private static final ClassDataRepository instance = new ClassDataRepository();
    private final ClassDataStore originalDataStore;
    private final ClassDataStore currentDataStore;
    private final List<Function<Class<?>, byte[]>> originalClassDataFuncList;

    public static ClassDataRepository getInstance() {
        return instance;
    }

    public static String getClassPathName(Class<?> clazz) {
        return "/" + clazz.getName().replace('.', '/') + ".class";
    }

    private ClassDataRepository() {
        String baseDir = getBaseDir();
        originalDataStore = new ClassDataStore(baseDir + File.separator + ORIGINAL_DIR);
        currentDataStore = new ClassDataStore(baseDir + File.separator + CURR_DIR);

        originalClassDataFuncList = Arrays.asList(
                this::loadClassDataFromResource,
                this::getClassDataFromOriginalStore,
                this::getClassDataFromMemory
        );
    }

    private String getBaseDir() {
        String baseDir = SystemConfig.get(KEY_CLASS_DATA_BASE_DIR);
        if (Utils.isBlank(baseDir))
            baseDir = Utils.wrapToRtError(
                    () -> Files.createTempDirectory(TMP_DIR).toFile().getAbsolutePath()
            );
        baseDir += File.separator + UUID.randomUUID();
        return baseDir;
    }

    public void saveClassData(Class<?> clazz, byte[] data) {
        saveClassData(
                Collections.singletonMap(clazz, data)
        );
    }

    public void saveClassData(Map<Class<?>, byte[]> classDataMap) {
        classDataMap.forEach(currentDataStore::save);
    }

    private byte[] loadClassDataFromResource(Class<?> clazz) {
        return TimeMeasureUtils.run(
                () -> {
                    logger.debug("Get class data from resource: {}", clazz.getName());
                    InputStream inputStream = clazz.getResourceAsStream(
                            getClassPathName(clazz)
                    );
                    if (inputStream != null)
                        return IOUtils.readBytes(inputStream);
                    logger.debug("No resource found for: {}", clazz.getName());
                    return null;
                },
                e -> {
                    logger.error("Get data from code source failed: {}", e, clazz.getName());
                    return null;
                },
                "loadClassDataFromResource: {}, {}",
                clazz.getName()
        );
    }

    private byte[] getClassDataFromOriginalStore(Class<?> clazz) {
        logger.debug("Get class data from original store: {}", clazz.getName());
        return originalDataStore.load(clazz);
    }

    private byte[] getClassDataFromCurrentStore(Class<?> clazz) {
        logger.debug("Get class data from current store: {}", clazz.getName());
        return currentDataStore.load(clazz);
    }

    private byte[] getClassDataFromMemory(Class<?> clazz) {
        return TimeMeasureUtils.run(
                () -> {
                    logger.debug("Get class data from memory: {}", clazz.getName());
                    GetClassDataTransformer transformer = new GetClassDataTransformer(clazz);
                    InstrumentationMgr.getInstance().retransform(transformer, clazz);
                    byte[] data = transformer.getData();
                    if (data == null)
                        data = new byte[0];
                    try {
                        originalDataStore.save(clazz, data);
                    } catch (Throwable t) {
                        logger.error("save class data failed: {}", t, clazz.getName());
                        return null;
                    }
                    return data;
                },
                t -> {
                    logger.error("Get class data failed.", t);
                    return null;
                },
                "getClassDataFromInstrumentation: {}, {}",
                clazz.getName()
        );
    }

    public byte[] getOriginalClassData(Class<?> clazz) {
        byte[] data;
        for (Function<Class<?>, byte[]> func : originalClassDataFuncList) {
            data = func.apply(clazz);
            if (data != null)
                return data;
        }
        throw new RuntimeException("No data found for class: " + clazz);
    }

    public byte[] getCurrentClassData(Class<?> clazz) {
        byte[] data = getClassDataFromCurrentStore(clazz);
        return data != null ? data : getOriginalClassData(clazz);
    }

    public String getOriginalClassDataPath(Class<?> clazz) {
        return originalDataStore.getClassDataFile(clazz).getAbsolutePath();
    }

    public String getCurrentClassDataPath(Class<?> clazz) {
        return currentDataStore.getClassDataFile(clazz).getAbsolutePath();
    }
}
