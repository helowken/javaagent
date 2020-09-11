package agent.common.config;

import java.util.Map;
import java.util.Objects;

public class TransformerConfig extends AbstractValidConfig {
    private String ref;
    private Map<String, Object> config;

    @Override
    public void validate() {
        validateNotBlank(ref, "Transformer reference");
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
        return Objects.equals(ref, that.ref) &&
                Objects.equals(config, that.config);
    }

    @Override
    public String toString() {
        return "TransformerConfig{" +
                "ref='" + ref + '\'' +
                ", config=" + config +
                '}';
    }

    @Override
    public int hashCode() {

        return Objects.hash(ref, config);
    }

}
