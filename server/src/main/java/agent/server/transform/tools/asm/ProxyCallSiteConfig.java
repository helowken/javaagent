package agent.server.transform.tools.asm;

class ProxyCallSiteConfig {
    final ProxyCallConfig callConfig;
    final Class<?> targetClass;
    final String targetMethodName;

    ProxyCallSiteConfig(ProxyCallConfig callConfig, Class<?> targetClass, String targetMethodName) {
        this.callConfig = callConfig;
        this.targetClass = targetClass;
        this.targetMethodName = targetMethodName;
    }
}
