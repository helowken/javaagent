package agent.server.utils.log.text;

import agent.server.utils.log.LogItem;

import java.util.Map;

public class TextLogItem implements LogItem {
    final Map<String, Object> paramValues;
    String content;

    public TextLogItem(Map<String, Object> paramValues) {
        this.paramValues = paramValues;
    }
}
