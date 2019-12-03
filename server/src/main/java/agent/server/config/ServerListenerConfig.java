package agent.server.config;

import agent.base.utils.Utils;

public class ServerListenerConfig {
    private String factoryClass;
    private String factoryMethod;
    private String listenerClass;

    public String getFactoryClass() {
        return factoryClass;
    }

    public void setFactoryClass(String factoryClass) {
        this.factoryClass = factoryClass;
    }

    public String getFactoryMethod() {
        return factoryMethod;
    }

    public void setFactoryMethod(String factoryMethod) {
        this.factoryMethod = factoryMethod;
    }

    public String getListenerClass() {
        return listenerClass;
    }

    public void setListenerClass(String listenerClass) {
        this.listenerClass = listenerClass;
    }

    public boolean useFactory() {
        return Utils.isNotBlank(factoryClass) && Utils.isNotBlank(factoryMethod);
    }

    public boolean useClass() {
        return Utils.isNotBlank(listenerClass);
    }

    @Override
    public String toString() {
        return "ServerListenerConfig{" +
                "factoryClass='" + factoryClass + '\'' +
                ", factoryMethod='" + factoryMethod + '\'' +
                ", listenerClass='" + listenerClass + '\'' +
                '}';
    }
}
