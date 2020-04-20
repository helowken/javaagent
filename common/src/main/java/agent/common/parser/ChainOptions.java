package agent.common.parser;

import agent.base.utils.Utils;

public class ChainOptions extends BasicOptions {
    public String chainClassStr = null;
    public String chainMethodStr = null;
    public String chainConstructorStr = null;
    public int chainLevel = -1;

    public boolean isUseChain() {
        return Utils.isNotBlank(chainClassStr) ||
                Utils.isNotBlank(chainMethodStr) ||
                Utils.isNotBlank(chainConstructorStr) ||
                chainLevel > -1;
    }
}
