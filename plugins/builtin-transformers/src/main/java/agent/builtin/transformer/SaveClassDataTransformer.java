package agent.builtin.transformer;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.TransformContext;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.dynamic.ClassCache;
import agent.server.transform.revision.ClassDataRepository;
import agent.server.transform.revision.ClassDataStore;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

public class SaveClassDataTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "sys_saveClassData";
    private static final String FILE_SUFFIX = ".class";
    private static final String KEY_OUTPUT_PATH = "outputPath";
    private static final String KEY_WITH_SELF = "withSelf";
    private static final String KEY_WITH_SUB_CLASS = "withSubClass";
    private static final String KEY_WITH_SUB_TYPE = "withSubType";
    private static final Map<String, Object> defaults = new HashMap<>();

    static {
        defaults.put(KEY_WITH_SELF, true);
        defaults.put(KEY_WITH_SUB_CLASS, false);
        defaults.put(KEY_WITH_SUB_TYPE, false);
    }

    private String outputPath;
    private boolean withSelf;
    private boolean withSubClass;
    private boolean withSubType;

    protected void doSetConfig(Map<String, Object> config) throws Exception {
        outputPath = (String) Optional.ofNullable(
                Utils.getConfigValue(config, KEY_OUTPUT_PATH)
        ).orElseThrow(
                () -> new RuntimeException("No " + KEY_OUTPUT_PATH + " found.")
        );
        withSelf = Utils.getConfigValue(config, KEY_WITH_SELF, defaults);
        withSubClass = Utils.getConfigValue(config, KEY_WITH_SUB_CLASS, defaults);
        withSubType = Utils.getConfigValue(config, KEY_WITH_SUB_TYPE, defaults);
    }

    @Override
    protected void transformMethod(Method method) throws Exception {
    }

    @Override
    public void transform(TransformContext transformContext, Class<?> clazz) throws Exception {
        Set<Class<?>> classSet = getClassSet(transformContext.context, clazz);
        classSet.forEach(
                savingClass -> ClassDataStore.save(
                        savingClass,
                        ClassDataRepository.getInstance().getClassData(savingClass),
                        currClass -> new File(
                                outputPath,
                                ClassDataStore.getFileName(currClass) + FILE_SUFFIX
                        )
                )
        );
    }

    private Set<Class<?>> getClassSet(String context, Class<?> clazz) {
        Set<Class<?>> classSet = new HashSet<>();
        if (withSelf)
            classSet.add(clazz);
        if (withSubType || withSubClass) {
            Map<String, Class<?>> nameToClass = ClassCache.getInstance().getSubClassMap(
                    context,
                    clazz.getName()
            );
            if (!withSubType)
                classSet.addAll(
                        ReflectionUtils.findSubClasses(
                                clazz,
                                nameToClass.values()
                        )
                );
            else
                classSet.addAll(
                        nameToClass.values()
                );
        }
        return classSet;
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
