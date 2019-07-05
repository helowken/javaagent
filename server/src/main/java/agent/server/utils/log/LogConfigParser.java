package agent.server.utils.log;

import java.util.Map;

public interface LogConfigParser {
    LogConfig parse(Map<String, Object> config, Map<String, Object> defaultValueMap);
}
