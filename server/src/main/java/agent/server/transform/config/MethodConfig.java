package agent.server.transform.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Arrays;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class MethodConfig {
    private String name;
    @JsonInclude(NON_NULL)
    private String[] argTypes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getArgTypes() {
        return argTypes;
    }

    public void setArgTypes(String[] argTypes) {
        this.argTypes = argTypes;
    }

    @Override
    public String toString() {
        return "MethodConfig{" +
                "name='" + name + '\'' +
                ", argTypes=" + Arrays.toString(argTypes) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MethodConfig that = (MethodConfig) o;
        return Objects.equals(name, that.name) &&
                Arrays.equals(argTypes, that.argTypes);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(name);
        result = 31 * result + Arrays.hashCode(argTypes);
        return result;
    }
}
