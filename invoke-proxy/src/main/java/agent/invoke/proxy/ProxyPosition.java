package agent.invoke.proxy;

public enum ProxyPosition {
    ON_BEFORE("onBefore"),
    ON_RETURNING("onReturning"),
    ON_THROWING("onThrowing"),
    ON_AFTER("onAfter");

    private final String name;

    ProxyPosition(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
