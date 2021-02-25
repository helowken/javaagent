package agent.base.utils;

import java.util.Objects;

public class HostAndPort {
    private static final String HOST_PORT_SEP = ":";
    public final String host;
    public final int port;

    public HostAndPort(String host, int port) {
        if (Utils.isBlank(host))
            throw new IllegalArgumentException("Invalid host.");
        if (port < 0)
            throw new IllegalArgumentException("Invalid port: " + port);
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostAndPort that = (HostAndPort) o;
        return port == that.port &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {

        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }

    public static HostAndPort parse(String addr, int defaultPort) {
        String[] hostPort = addr.split(HOST_PORT_SEP);
        if (hostPort.length > 2)
            throw new RuntimeException("Invalid address: " + addr + ", it must be \"host[:port]\"");
        String host = hostPort[0].trim();
        if (Utils.isBlank(host))
            throw new RuntimeException("Invalid host: " + addr);
        int port = defaultPort;
        if (hostPort.length == 2) {
            String portStr = hostPort[1].trim();
            if (Utils.isBlank(portStr))
                throw new RuntimeException("Invalid port: " + addr);
            port = Utils.parseInt(
                    portStr,
                    "Port " + portStr
            );
        }
        return new HostAndPort(host, port);
    }
}
