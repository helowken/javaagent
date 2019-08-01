package test.tomcat;


import org.apache.catalina.startup.Bootstrap;
import org.junit.Test;

import java.util.Map;

import static agent.base.utils.ReflectionUtils.getFieldValue;
import static agent.base.utils.ReflectionUtils.setStaticFieldValue;
import static org.junit.Assert.assertNotNull;

public class TomcatTest {
    @Test
    public void test() throws Exception {
        Bootstrap bootstrap = new Bootstrap();
        new Thread(() -> {
            try {
                Thread.sleep(10000);

                Object catalina = getFieldValue("catalinaDaemon", bootstrap);
                assertNotNull(catalina);

                Object server = getFieldValue("server", catalina);
                assertNotNull(server);

                Object[] services = getFieldValue("services", server);
                assertNotNull(services);

                Object engine = getEngine(services);
                assertNotNull(engine);

                Map childrenMap = getFieldValue("children", engine);
                assertNotNull(childrenMap);

                Object host = getHost(childrenMap);
                assertNotNull(host);

                childrenMap = getFieldValue("children", host);
                assertNotNull(childrenMap);

                Object webappLoader = getWebappLoader(childrenMap);
                assertNotNull(webappLoader);

                Object webappClassLoader = getFieldValue("classLoader", webappLoader);
                assertNotNull(webappClassLoader);
                System.out.println(webappClassLoader);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        setStaticFieldValue(Bootstrap.class, "daemon", bootstrap);
        bootstrap.init();
        Bootstrap.main(new String[]{"start"});
    }

    private Object getWebappLoader(Map childrenMap) throws Exception {
        final String contextClassName = "org.apache.catalina.core.StandardContext";
        for (Object child : childrenMap.values()) {
            if (child.getClass().getName().equals(contextClassName)) {
                return getFieldValue(
                        contextClassName,
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
                        standardServiceClassName,
                        "container",
                        service
                );
            }
        }
        return null;
    }

}
