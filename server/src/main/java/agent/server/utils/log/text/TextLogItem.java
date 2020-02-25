package agent.server.utils.log.text;

import agent.server.utils.log.AbstractLogItem;

public class TextLogItem extends AbstractLogItem {
    final String content;

    public TextLogItem(String content) {
        this.content = content;
    }

    public int getSize() {
        return this.content.length();
    }
}
