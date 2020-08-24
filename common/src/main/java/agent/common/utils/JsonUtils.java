package agent.common.utils;

import agent.base.utils.ReflectionUtils;
import agent.base.utils.TypeObject;
import agent.base.utils.Utils;

import java.io.InputStream;
import java.lang.reflect.Type;

public class JsonUtils {
    public static final String JSON_DELEGATE_CLASS = "agent.tools.json.JSONDelegate";
    private static final DelegateClassItem item = DelegateClassItem.getInstance();

    public static <T> T read(String content) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        item.getDelegateClass(JSON_DELEGATE_CLASS),
                        "read",
                        new Class[]{String.class},
                        content
                )
        );
    }

    public static <T> T read(InputStream inputStream, TypeObject typeObject) {
        return Utils.wrapToRtError(
                () -> ReflectionUtils.invokeStatic(
                        item.getDelegateClass(JSON_DELEGATE_CLASS),
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
                        item.getDelegateClass(JSON_DELEGATE_CLASS),
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
                        item.getDelegateClass(JSON_DELEGATE_CLASS),
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
                        item.getDelegateClass(JSON_DELEGATE_CLASS),
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
                        item.getDelegateClass(JSON_DELEGATE_CLASS),
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

}
