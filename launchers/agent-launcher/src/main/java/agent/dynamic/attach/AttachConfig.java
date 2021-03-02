package agent.dynamic.attach;

import java.util.List;

class AttachConfig {
    private JarPathAndOption jarPathAndOption;
    private List<JavaEndpoint> javaEndpointList;

    public JarPathAndOption getJarPathAndOption() {
        return jarPathAndOption;
    }

    public void setJarPathAndOption(JarPathAndOption jarPathAndOption) {
        this.jarPathAndOption = jarPathAndOption;
    }

    public List<JavaEndpoint> getJavaEndpointList() {
        return javaEndpointList;
    }

    public void setJavaEndpointList(List<JavaEndpoint> javaEndpointList) {
        this.javaEndpointList = javaEndpointList;
    }
}
