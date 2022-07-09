package agent.dynamic.attach;

import java.util.List;

class AttachConfig {
    private JarPathAndOption jarPathAndOption;
    private List<JavaEndpoint> javaEndpointList;
    private boolean legacy;
    private boolean verbose;

    JarPathAndOption getJarPathAndOption() {
        return jarPathAndOption;
    }

    void setJarPathAndOption(JarPathAndOption jarPathAndOption) {
        this.jarPathAndOption = jarPathAndOption;
    }

    List<JavaEndpoint> getJavaEndpointList() {
        return javaEndpointList;
    }

    void setJavaEndpointList(List<JavaEndpoint> javaEndpointList) {
        this.javaEndpointList = javaEndpointList;
    }

    boolean isLegacy() {
        return legacy;
    }

    void setLegacy(boolean legacy) {
        this.legacy = legacy;
    }

    boolean isVerbose() {
        return verbose;
    }

    void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }
}
