package agent.server.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

public class JSONUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T read(String content) throws IOException {
        return read(content, new TypeReference<T>() {
        });
    }

    public static <T> T read(String content, TypeReference<T> typeReference) throws IOException {
        return objectMapper.readValue(content, typeReference);
    }

    public static String writeAsString(Object o) throws IOException {
        return objectMapper.writeValueAsString(o);
    }
}
