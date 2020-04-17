package agent.builtin.transformer;

import agent.base.utils.Utils;
import agent.server.transform.TransformContext;
import agent.server.transform.impl.AbstractConfigTransformer;
import agent.server.transform.impl.invoke.DestInvoke;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SaveClassDataTransformer extends AbstractConfigTransformer {
    public static final String REG_KEY = "@saveClassData";
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
    protected void transformDestInvoke(DestInvoke destInvoke) throws Exception {
    }

    @Override
    public void transform(TransformContext transformContext) throws Exception {
//        ClassFinder classFinder = getClassFinder();
//        transformContext.getTargetClassSet().forEach(
//                clazz -> getClassSet(
//                        getTransformShareInfo().getClassCache(),
//                        classFinder.findClassLoader(
//                                transformContext.getContext()
//                        ),
//                        clazz
//                ).forEach(
//                        savingClass -> ClassDataStore.save(
//                                savingClass,
//                                ClassDataRepository.getInstance().getClassData(savingClass),
//                                currClass -> new File(
//                                        outputPath,
//                                        ClassDataStore.getFileName(currClass) + FILE_SUFFIX
//                                )
//                        )
//                )
//        );
    }

//    private Set<Class<?>> getClassSet(ClassCache classCache, ClassLoader loader, Class<?> baseClass) {
//        Set<Class<?>> classSet = new HashSet<>();
//        if (withSelf)
//            classSet.add(baseClass);
//        if (withSubClass)
//            classSet.addAll(
//                    classCache.getSubClasses(loader, baseClass, false)
//            );
//        if (withSubType)
//            classSet.addAll(
//                    classCache.getSubTypes(loader, baseClass, false)
//            );
//        return classSet;
//    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }
}
