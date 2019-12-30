package test.server.transform;

import agent.server.transform.ContextClassLoaderMgr;
import org.junit.Before;
import org.junit.Test;
import test.server.AbstractTest;

import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClasspathTest extends AbstractTest {
    private static final String context1 = "test";
    private static final String context2 = "test2";
    private static final ContextClassLoaderMgr mgr = ContextClassLoaderMgr.getInstance();

    @Before
    public void before() {
//        classFinder.setContextLoader(context1);
//        classFinder.setContextLoader(context2);
    }

    @Test
    public void testAddCP() throws Exception {
        doAdd();
    }

    @Test
    public void testRemoveCP() throws Exception {
        doAdd();
        mgr.getContextToClasspathSet()
                .forEach((context, classpathSet) ->
                        classpathSet.forEach(classpath ->
                                mgr.removeClasspath(context, classpath)
                        )
                );
        assertTrue(mgr.getContextToClasspathSet().isEmpty());
    }

    private void doAdd() throws Exception {
        Map<String, Set<URL>> contextToClasspath = new HashMap<>();
        contextToClasspath.put(context1, new HashSet<>(Arrays.asList(
                new URL("http://localhost:8080/lib/"),
                new URL("file:///home/xxx/aa/")
        )));
        contextToClasspath.put(context2, new HashSet<>(Arrays.asList(
                new URL("http://localhost:8081/lib/"),
                new URL("file:///home/ccc/ddd/")
        )));
        contextToClasspath.forEach((context, classpathSet) ->
                classpathSet.forEach(classpath ->
                        mgr.addClasspath(context, classpath)
                )
        );
        assertEquals(contextToClasspath, mgr.getContextToClasspathSet());
    }
}
