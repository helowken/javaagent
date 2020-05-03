package agent.common.config;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static agent.base.utils.AssertUtils.assertNotNull;

abstract class AbstractAgentConfig implements AgentConfig {
    void validateIfNotNull(AgentConfig... configs) {
        if (configs != null)
            Stream.of(configs)
                    .filter(Objects::nonNull)
                    .forEach(AgentConfig::validate);
    }

    void validate(Object v, String field) {
        assertNotNull(v, field + " is null.");
        if (v instanceof Collection) {
            Collection<Object> vs = (Collection) v;
            vs.forEach(
                    el -> {
                        if (el instanceof AgentConfig)
                            ((AgentConfig) el).validate();
                    }
            );
        }
    }
}
