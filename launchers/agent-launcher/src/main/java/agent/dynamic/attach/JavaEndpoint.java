package agent.dynamic.attach;

class JavaEndpoint {
    final String name;
    final String pid;
    final int port;

    JavaEndpoint(String name, String pid, int port) {
        this.name = name;
        this.pid = pid;
        this.port = port;
    }

    @Override
    public String toString() {
        return "name='" + name + '\'' +
                (pid != null ? ", pid='" + pid + '\'' : "") +
                ", port=" + port;

    }
}
