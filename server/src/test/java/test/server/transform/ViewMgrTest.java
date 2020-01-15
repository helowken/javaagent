package test.server.transform;

import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.impl.ViewMgr;
import org.junit.Test;
import test.server.TestProxy;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class ViewMgrTest extends AbstractViewTest {

    @Test
    public void testViewContext() {
        validateViewContext(null, Arrays.asList(contextA, contextB));
        validateViewContext(".*A", Collections.singletonList(contextA));
        validateViewContext(".*B", Collections.singletonList(contextB));
    }

    @Test
    public void testViewClass() {
        Map<String, List<String>> expectedContextToClasses = new TreeMap<>();
        populateContextAToClasses(expectedContextToClasses);
        populateContextBToClasses(expectedContextToClasses);
        validateViewClass(
                null,
                Arrays.asList(contextA, contextB),
                null,
                expectedContextToClasses
        );

        expectedContextToClasses = new TreeMap<>();
        populateContextAToClasses(expectedContextToClasses);
        validateViewClass(
                ".*A",
                Collections.singletonList(contextA),
                null,
                expectedContextToClasses
        );

        validateViewClass(
                ".*A",
                Collections.singletonList(contextA),
                ".*A2",
                Collections.singletonMap(
                        contextA,
                        Collections.singletonList(
                                A2.class.getName()
                        )
                )
        );
    }

    @Test
    public void testViewInvoke() {
        Set<String> invokeSet = new TreeSet<>();
        Stream.of(
                A.class.getDeclaredMethods()
        ).map(InvokeDescriptorUtils::getFullDescriptor)
                .forEach(invokeSet::add);

        Stream.of(
                A.class.getDeclaredConstructors()
        ).map(InvokeDescriptorUtils::getFullDescriptor)
                .forEach(invokeSet::add);

        Map map = (Map) ViewMgr.create(ViewMgr.VIEW_INVOKE, ".*A", ".*A", null);
        map = (Map) map.get(contextA);
        Collection<String> invokes = (Collection) map.get(A.class.getName());
        assertEquals(
                new TreeSet<>(invokes),
                invokeSet
        );

        invokeSet = new TreeSet<>();
        Stream.of(
                A.class.getDeclaredMethods()
        ).map(InvokeDescriptorUtils::getFullDescriptor)
                .forEach(invokeSet::add);

        map = (Map) ViewMgr.create(ViewMgr.VIEW_INVOKE, ".*A", ".*A", "[^<].*");
        map = (Map) map.get(contextA);
        invokes = (Collection) map.get(A.class.getName());
        assertEquals(
                new TreeSet<>(invokes),
                invokeSet
        );
    }

    @Test
    public void testViewProxy() {
        Map map = (Map) ViewMgr.create(ViewMgr.VIEW_PROXY, ".*A", ".*A", null);
        map = (Map) map.get(contextA);
        map = (Map) map.get(A.class.getName());

        Map methodResult = Stream.of(
                A.class.getDeclaredMethods()
        ).collect(
                Collectors.toMap(
                        InvokeDescriptorUtils::getFullDescriptor,
                        method -> newProxyMap("onBefore", "onReturning", "onThrowing", "onAfter")
                )
        );
        Map constructorResult = Stream.of(
                A.class.getDeclaredConstructors()
        ).collect(
                Collectors.toMap(
                        InvokeDescriptorUtils::getFullDescriptor,
                        method -> newProxyMap("onReturning", "onThrowing", "onAfter")
                )
        );
        Map allResult = new TreeMap(methodResult);
        allResult.putAll(constructorResult);
        assertEquals(map, allResult);

        map = (Map) ViewMgr.create(ViewMgr.VIEW_PROXY, ".*A", ".*A", "[^<].*");
        map = (Map) map.get(contextA);
        map = (Map) map.get(A.class.getName());
        assertEquals(map, methodResult);

        map = (Map) ViewMgr.create(ViewMgr.VIEW_PROXY, ".*A", ".*A", "<.*");
        map = (Map) map.get(contextA);
        map = (Map) map.get(A.class.getName());
        assertEquals(map, constructorResult);
    }

    private Map<String, List<String>> newProxyMap(String... proxyMethodNames) {
        return new TreeMap<>(
                Stream.of(
                        proxyMethodNames
                ).collect(
                        Collectors.toMap(
                                key -> key,
                                key -> Collections.singletonList(
                                        Utils.wrapToRtError(
                                                () -> ReflectionUtils.findFirstMethod(TestProxy.class, key).toString()
                                        )
                                )
                        )
                )
        );
    }

    private void populateContextAToClasses(Map<String, List<String>> expectedContextToClasses) {
        expectedContextToClasses.put(
                contextA,
                Arrays.asList(
                        A.class.getName(),
                        A2.class.getName()
                )
        );
    }

    private void populateContextBToClasses(Map<String, List<String>> expectedContextToClasses) {
        expectedContextToClasses.put(
                contextB,
                Arrays.asList(
                        B.class.getName(),
                        B2.class.getName()
                )
        );
    }

    private void validateViewClass(String contextRegexp, Collection<String> expectedContexts, String classRegexp,
                                   Map<String, List<String>> expectedContextToClasses) {
        Map<String, List<String>> contextToClasses = (Map) ViewMgr.create(ViewMgr.VIEW_CLASS, contextRegexp, classRegexp, null);
        assertEquals(
                new TreeSet<>(expectedContexts),
                new TreeSet<>(contextToClasses.keySet())
        );
        assertEquals(
                expectedContextToClasses,
                new TreeMap<>(contextToClasses)
        );
    }

    private void validateViewContext(String contextRegexp, Collection<String> contexts) {
        Collection<String> contextSet = new TreeSet<>(
                (Collection) ViewMgr.create(ViewMgr.VIEW_CONTEXT, contextRegexp, null, null)
        );
        assertEquals(
                new TreeSet<>(contexts),
                contextSet
        );
    }


}
