package agent.builtin.tools.result;

public class TraceResultOptions extends ResultOptions {
    public boolean displayTime = true;
    public boolean displayArgs = true;
    public boolean displayReturnValue = true;
    public boolean displayError = true;
    public int contentMaxSize = 50;
    public int headRows = 0;
    public int tailRows = 1;
}
