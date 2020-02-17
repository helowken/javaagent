package agent.server.transform.cache;

public class NotInterfaceClassFilter implements ClassFilter {
    public static final NotInterfaceClassFilter instance = new NotInterfaceClassFilter();

    public static NotInterfaceClassFilter getInstance() {
        return instance;
    }

    private NotInterfaceClassFilter() {
    }

    @Override
    public boolean accept(Class<?> clazz) {
        return !clazz.isInterface();
    }
}
