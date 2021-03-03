package agent.common.config;

import agent.base.struct.annotation.PojoProperty;

import java.util.Map;
import java.util.Objects;

public class TransformerConfig extends AbstractValidConfig {
    @PojoProperty(index = 0)
    private String id;
    @PojoProperty(index = 1)
    private String ref;
    @PojoProperty(index = 2)
    private Map<String, Object> config;

    @Override
    public void validate() {
        validateNotBlank(id, "Transformer id");
        validateNotBlank(ref, "Transformer reference");
        TidUtils.validate(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
        return Objects.equals(id, that.id) &&
                Objects.equals(ref, that.ref) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, ref, config);
    }

    @Override
    public String toString() {
        return "TransformerConfig{" +
                "id='" + id + '\'' +
                ", ref='" + ref + '\'' +
                ", config=" + config +
                '}';
    }
}
