package agent.server.utils.log.binary;

import agent.base.utils.LockObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BinaryConverterRegistry {
    private static final Map<Class<?>, BinaryConverter> typeToConverter = new HashMap<>();
    private static final LockObject typeToConverterLock = new LockObject();

    static {
        reg(byte[].class, v -> (byte[]) v);
        reg(String.class, v -> ((String) v).getBytes());
    }

    public static void reg(Class<?> type, BinaryConverter converter) {
        typeToConverterLock.sync(lock ->
                typeToConverter.put(type, converter)
        );
    }

    public static BinaryConverter getConverter(Class<?> type) {
        return typeToConverterLock.syncValue(lock ->
                Optional.ofNullable(typeToConverter.get(type))
                        .orElseThrow(() -> new RuntimeException("No binary converter found for class: " + type))
        );
    }
}
