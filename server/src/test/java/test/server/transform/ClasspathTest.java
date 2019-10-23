package test.server.transform;

import agent.server.transform.TransformMgr;
import org.junit.Before;
import org.junit.Test;
import test.server.AbstractServerTest;

import java.net.URL;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ClasspathTest extends AbstractServerTest {
    private static final String context1 = "test";
    private static final String context2 = "test2";

    @Before
    public void before() {
        classFinder.setContextLoader(context1);
        classFinder.setContextLoader(context2);
    }

    @Test
    public void testAddCP() throws Exception {
        doAdd();
    }

    @Test
    public void testRemoveCP() throws Exception {
        doAdd();
        TransformMgr.getInstance()
                .getContextToClasspathSet()
                .forEach((context, classpathSet) ->
                        classpathSet.forEach(classpath ->
                                TransformMgr.getInstance().removeClasspath(context, classpath)
                        )
                );
        assertTrue(TransformMgr.getInstance().getContextToClasspathSet().isEmpty());
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
                        TransformMgr.getInstance().addClasspath(context, classpath)
                )
        );

        assertEquals(contextToClasspath, TransformMgr.getInstance().getContextToClasspathSet());
    }
}
