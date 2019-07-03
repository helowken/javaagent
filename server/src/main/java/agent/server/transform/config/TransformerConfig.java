package agent.server.transform.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public class TransformerConfig {
    @JsonInclude(NON_NULL)
    private String implClass;
    @JsonInclude(NON_NULL)
    private String ref;
    @JsonInclude(NON_NULL)
    private Map<String, Object> config;

    public String getImplClass() {
        return implClass;
    }

    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Map<String, Object> getConfig() {
        return config;
    }

    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransformerConfig that = (TransformerConfig) o;
        return Objects.equals(implClass, that.implClass) &&
                Objects.equals(ref, that.ref) &&
                Objects.equals(config, that.config);
    }

    @Override
    public String toString() {
        return "TransformerConfig{" +
                "implClass='" + implClass + '\'' +
                ", ref='" + ref + '\'' +
                ", config=" + config +
                '}';
    }

    @Override
    public int hashCode() {

        return Objects.hash(implClass, ref, config);
    }


}
