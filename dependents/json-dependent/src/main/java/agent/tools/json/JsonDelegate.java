package agent.tools.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;

public class JsonDelegate {
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

    public static <T> T read(InputStream inputStream, Type type) throws IOException {
        return objectMapper.readValue(
                inputStream,
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
        return writeAsString(o, false);
    }

    public static String writeAsString(Object o, boolean pretty) throws IOException {
        return pretty ?
                objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(o) :
                objectMapper.writeValueAsString(o);
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
