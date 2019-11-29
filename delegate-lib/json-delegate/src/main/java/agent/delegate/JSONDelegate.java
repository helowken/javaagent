package agent.delegate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.Type;

public class JSONDelegate {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static <T> T read(String content) throws IOException {
        return objectMapper.readValue(
                content,
                new TypeReference<Object>() {
                }
        );
    }

    public static <T> T read(String content, Type type) throws IOException {
        return objectMapper.readValue(
                content,
                newTypeReference(type)
        );
    }

    public static <T> T convert(Object content, Type type) {
        return objectMapper.convertValue(
                content,
                newTypeReference(type)
        );
    }

    public static String writeAsString(Object o) throws IOException {
        return objectMapper.writeValueAsString(o);
    }

    private static TypeReference newTypeReference(final Type type) {
        return new TypeReference<Object>() {
            @Override
            public Type getType() {
                return type;
            }
        };
    }
}
