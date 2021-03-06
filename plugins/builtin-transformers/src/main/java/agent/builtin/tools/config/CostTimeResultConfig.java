package agent.builtin.tools.config;

public class CostTimeResultConfig extends AbstractResultConfig {
    private boolean invoke;

    public boolean isInvoke() {
        return invoke;
    }

    public void setInvoke(boolean invoke) {
        this.invoke = invoke;
    }
}
