package agent.hook.tomcat;

import agent.hook.plugin.AbstractClassFinder;

import java.util.Map;

import static agent.base.utils.AssertUtils.assertNotNull;
import static agent.base.utils.ReflectionUtils.getFieldValue;
import static agent.base.utils.ReflectionUtils.invoke;

public class TomcatClassFinder extends AbstractClassFinder {
    @Override
    protected void doInit(Object app, Map<String, ClassLoader> contextToLoader) throws Exception {
        Object catalina = getFieldValue("catalinaDaemon", app);
        assertNotNull(catalina, "catalinaDaemon of app is null.");

        Object server = getFieldValue("server", catalina);
        assertNotNull(server, "server of catalinaDaemon is null.");

        Object[] services = getFieldValue("services", server);
        assertNotNull(services, "services of server is null.");

        Object engine = getEngine(services);
        assertNotNull(engine, "engine of services is null.");

        Map childrenMap = getFieldValue("children", engine);
        assertNotNull(childrenMap, "children of engine is null.");

        Object host = getHost(childrenMap);
        assertNotNull(host, "host of children is null.");

        childrenMap = getFieldValue("children", host);
        assertNotNull(childrenMap, "children of host is null.");

        Object webappLoader = getWebappLoader(childrenMap);
        assertNotNull(webappLoader, "webappLoader of children is null.");

        ClassLoader webappClassLoader = getFieldValue("classLoader", webappLoader);
        assertNotNull(webappClassLoader, "webappClassLoader of webappLoader is null.");

        String contextPath = invoke("getContextName", webappClassLoader);
        assertNotNull(contextPath, "contextPath of webappClassLoader is null.");

        contextToLoader.put(contextPath, webappClassLoader);
    }

    private Object getWebappLoader(Map childrenMap) throws Exception {
        final String contextClassName = "org.apache.catalina.core.StandardContext";
        for (Object child : childrenMap.values()) {
            if (child.getClass().getName().equals(contextClassName)) {
                return getFieldValue(
                        "loader",
                        child
                );
            }
        }
        return null;
    }

    private Object getHost(Map childrenMap) {
        final String hostClassName = "org.apache.catalina.core.StandardHost";
        for (Object child : childrenMap.values()) {
            if (child.getClass().getName().equals(hostClassName)) {
                return child;
            }
        }
        return null;
    }

    private Object getEngine(Object[] services) throws Exception {
        final String standardServiceClassName = "org.apache.catalina.core.StandardService";
        for (Object service : services) {
            if (service.getClass().getName().equals(standardServiceClassName)) {
                return getFieldValue(
                        "container",
                        service
                );
            }
        }
        return null;
    }
}
