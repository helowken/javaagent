package agent.common.utils;

import agent.base.utils.*;

import java.io.InputStream;
import java.lang.reflect.Type;

public class JSONUtils {
    private static final String KEY_DELEGATE_LIB_DIR = "delegate.lib.dir";
    private static final String DELEGATE_CLASS_NAME = "agent.delegate.JSONDelegate";
    private static ClassLoader loader;
    private static Class<?> delegateClass;

    public static <T> T read(String content) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        getDelegateClass(),
                        "read",
                        new Class[]{String.class},
                        content
                )
        );
    }

    public static <T> T read(InputStream inputStream, TypeObject typeObject) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        getDelegateClass(),
                        "read",
                        new Class[]{
                                InputStream.class,
                                Type.class
                        },
                        inputStream,
                        typeObject.type
                )
        );
    }

    public static <T> T read(String content, TypeObject typeObject) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        getDelegateClass(),
                        "read",
                        new Class[]{
                                String.class,
                                Type.class
                        },
                        content,
                        typeObject.type
                )
        );
    }

    public static <T> T convert(Object content, TypeObject typeObject) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        getDelegateClass(),
                        "convert",
                        new Class[]{
                                Object.class,
                                Type.class
                        },
                        content,
                        typeObject.type
                )
        );
    }

    public static String writeAsString(Object o) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        getDelegateClass(),
                        "writeAsString",
                        new Class[]{
                                Object.class
                        },
                        o
                )
        );
    }

    public static String writeAsString(Object o, boolean pretty) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        getDelegateClass(),
                        "writeAsString",
                        new Class[]{
                                Object.class,
                                boolean.class
                        },
                        o,
                        pretty
                )
        );
    }

    private static synchronized ClassLoader getLoader() throws Exception {
        if (loader == null)
            loader = ClassLoaderUtils.newURLClassLoader(
                    JSONUtils.class.getClassLoader(),
                    FileUtils.splitPathStringToPathArray(
                            SystemConfig.splitToSet(KEY_DELEGATE_LIB_DIR),
                            SystemConfig.getBaseDir()
                    )
            );
        return loader;
    }

    private static synchronized Class<?> getDelegateClass() throws Exception {
        if (delegateClass == null)
            delegateClass = getLoader().loadClass(DELEGATE_CLASS_NAME);
        return delegateClass;
    }
}
