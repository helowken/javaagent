package agent.launcher.client;

import agent.base.utils.HostAndPort;
import agent.base.utils.Utils;

import java.util.HashSet;
import java.util.Set;

class AddressUtils {
    private static final String ADDRESS_SEP = ",";
    static final HostAndPort defaultAddress = new HostAndPort("127.0.0.1", 10100);

    static HostAndPort parseAddr(String address) {
        return Utils.isBlank(address) ?
                defaultAddress :
                HostAndPort.parse(
                        address,
                        defaultAddress.port
                );
    }

    static Set<HostAndPort> parseAddrs(String addrString) {
        Set<HostAndPort> hostAndPorts = new HashSet<>();
        if (Utils.isBlank(addrString))
            hostAndPorts.add(defaultAddress);
        else {
            String[] addrs = addrString.split(ADDRESS_SEP);
            for (String addr : addrs) {
                if (Utils.isNotBlank(addr))
                    hostAndPorts.add(
                            parseAddr(addr)
                    );
            }
        }
        return hostAndPorts;
    }
}
