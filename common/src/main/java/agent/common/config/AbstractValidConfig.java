package agent.common.config;

import agent.base.utils.Utils;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

import static agent.base.utils.AssertUtils.assertNotNull;
import static agent.base.utils.AssertUtils.assertTrue;

abstract class AbstractValidConfig implements ValidConfig {
    void validateIfNotNull(ValidConfig... configs) {
        if (configs != null)
            Stream.of(configs)
                    .filter(Objects::nonNull)
                    .forEach(ValidConfig::validate);
    }

    void validateNotNull(Object v, String field) {
        assertNotNull(v, field + " is null.");
        if (v instanceof Collection) {
            Collection<Object> vs = (Collection) v;
            vs.forEach(
                    el -> {
                        if (el instanceof ValidConfig)
                            ((ValidConfig) el).validate();
                    }
            );
        }
    }

    void validateNotBlank(String v, String field) {
        assertTrue(
                Utils.isNotBlank(v),
                field + " is blank."
        );
    }
}
