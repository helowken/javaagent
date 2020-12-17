package agent.builtin.transformer.utils;

import java.util.Map;

public interface ValueConverter {
    Map<String, Object> convertArg(int index, Class<?> clazz, Object value);

    Map<String, Object> convertReturnValue(Class<?> clazz, Object value);

    Map<String, Object> convertError(Throwable error);

    Object getMetadata();

    void destroy();
}
