package agent.common.parser;

import agent.base.utils.Utils;

public class ChainFilterOptions extends BasicFilterOptions {
    public String chainMatchClassStr = null;
    public String chainMatchMethodStr = null;
    public String chainMatchConstructorStr = null;
    public int chainSearchLevel = -1;
    public String chainSearchClassStr = null;
    public String chainSearchMethodStr = null;
    public String chainSearchConstructorStr = null;

    public boolean isUseChain() {
        return Utils.isNotBlank(chainMatchClassStr) ||
                Utils.isNotBlank(chainMatchMethodStr) ||
                Utils.isNotBlank(chainMatchConstructorStr);
    }
}
