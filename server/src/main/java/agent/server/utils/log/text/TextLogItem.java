package agent.server.utils.log.text;

import agent.server.utils.log.AbstractLogItem;

import java.util.Map;

public class TextLogItem extends AbstractLogItem {
    final Map<String, Object> paramValues;
    String content;

    public TextLogItem(Map<String, Object> paramValues) {
        this.paramValues = paramValues;
    }
}
