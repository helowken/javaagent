package agent.server.transform.search.filter;

public class NotInterfaceFilter extends AbstractClassFilter {
    private static final NotInterfaceFilter instance = new NotInterfaceFilter();

    public static NotInterfaceFilter getInstance() {
        return instance;
    }

    private NotInterfaceFilter() {
    }

    @Override
    public boolean accept(Class<?> clazz) {
        return !clazz.isInterface();
    }
}
