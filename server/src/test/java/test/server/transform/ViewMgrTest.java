package test.server.transform;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("unchecked")
public class ViewMgrTest extends AbstractInfoTest {
//
//    @Test
//    public void testViewClass() {
//        Map<String, List<String>> expectedContextToClasses = new TreeMap<>();
//        populateContextAToClasses(expectedContextToClasses);
//        populateContextBToClasses(expectedContextToClasses);
//        validateViewClass(
//                null,
//                Arrays.asList(contextA, contextB),
//                null,
//                expectedContextToClasses
//        );
//
//        expectedContextToClasses = new TreeMap<>();
//        populateContextAToClasses(expectedContextToClasses);
//        validateViewClass(
//                "*A",
//                Collections.singletonList(contextA),
//                null,
//                expectedContextToClasses
//        );
//
//        validateViewClass(
//                "*A",
//                Collections.singletonList(contextA),
//                "*A2",
//                Collections.singletonMap(
//                        contextA,
//                        Collections.singletonList(
//                                A2.class.getName()
//                        )
//                )
//        );
//    }
//
//    @Test
//    public void testViewInvoke() {
//        Set<String> invokeSet = new TreeSet<>();
//        Stream.of(
//                A.class.getDeclaredMethods()
//        ).map(AsmTestUtils::methodToString)
//                .forEach(invokeSet::add);
//
//        Stream.of(
//                A.class.getDeclaredConstructors()
//        ).map(AsmTestUtils::constructorToString)
//                .forEach(invokeSet::add);
//
//        Map map = (Map) ViewMgr.create(ViewMgr.VIEW_INVOKE, "*A", null, null);
//        map = (Map) map.get(contextA);
//        Collection<String> invokes = (Collection) map.get(A.class.getName());
//        assertEquals(
//                new TreeSet<>(invokes),
//                invokeSet
//        );
//
//        invokeSet = new TreeSet<>();
//        Stream.of(
//                A.class.getDeclaredMethods()
//        ).map(AsmTestUtils::methodToString)
//                .forEach(invokeSet::add);
//
////        map = (Map) ViewMgr.create(ViewMgr.VIEW_INVOKE, "*A", "*A", "[^<]*", null);
////        map = (Map) map.get(contextA);
////        invokes = (Collection) map.get(A.class.getName());
////        assertEquals(
////                new TreeSet<>(invokes),
////                invokeSet
////        );
//    }
//
//    @Test
//    public void testViewProxy() {
//        Map map = (Map) ViewMgr.create(ViewMgr.VIEW_PROXY, "*A", "*A", null, null);
//        map = (Map) map.get(contextA);
//        map = new TreeMap<>((Map) map.get(A.class.getName()));
//        System.out.println(map);
//
//        Map methodResult = Stream.of(
//                A.class.getDeclaredMethods()
//        ).collect(
//                Collectors.toMap(
//                        AsmTestUtils::methodToString,
//                        method -> newProxyMap("onBefore", "onReturning", "onThrowing", "onAfter")
//                )
//        );
//        Map constructorResult = Stream.of(
//                A.class.getDeclaredConstructors()
//        ).collect(
//                Collectors.toMap(
//                        AsmTestUtils::constructorToString,
//                        method -> newProxyMap("onBefore", "onReturning", "onThrowing", "onAfter")
//                )
//        );
//        Map allResult = new TreeMap(methodResult);
//        allResult.putAll(constructorResult);
//        assertEquals(allResult, map);
//
////        map = (Map) ViewMgr.create(ViewMgr.VIEW_PROXY, "*A", "*A", "[^<]*", null);
////        map = (Map) map.get(contextA);
////        map = (Map) map.get(A.class.getName());
////        assertEquals(map, methodResult);
////
////        map = (Map) ViewMgr.create(ViewMgr.VIEW_PROXY, "*A", "*A", "<*", null);
////        map = (Map) map.get(contextA);
////        map = (Map) map.get(A.class.getName());
////        assertEquals(map, constructorResult);
//    }
//
//    private Map<String, List<String>> newProxyMap(String... proxyMethodNames) {
//        return new TreeMap<>(
//                Stream.of(
//                        proxyMethodNames
//                ).collect(
//                        Collectors.toMap(
//                                key -> key,
//                                key -> Collections.singletonList(
//                                        TestAnnotationConfigTransformer.REG_KEY
//                                )
////                                key -> Collections.singletonList(
////                                        Utils.wrapToRtError(
////                                                () -> ReflectionUtils.findFirstMethod(TestProxy.class, key).toString()
////                                        )
////                                )
//                        )
//                )
//        );
//    }
//
//    private void populateContextAToClasses(Map<String, List<String>> expectedContextToClasses) {
//        expectedContextToClasses.put(
//                contextA,
//                Arrays.asList(
//                        A.class.getName(),
//                        A2.class.getName()
//                )
//        );
//    }
//
//    private void populateContextBToClasses(Map<String, List<String>> expectedContextToClasses) {
//        expectedContextToClasses.put(
//                contextB,
//                Arrays.asList(
//                        B.class.getName(),
//                        B2.class.getName()
//                )
//        );
//    }
//
//    private void validateViewClass(String contextRegexp, String classRegexp,
//                                   Map<String, List<String>> expectedContextToClasses) {
//        Map<String, List<String>> contextToClasses = (Map) ViewMgr.create(ViewMgr.VIEW_CLASS, classRegexp, null, null);
//        assertEquals(
//                expectedContextToClasses,
//                sortMap(contextToClasses)
//        );
//    }
//
//    private Map<String, List<String>> sortMap(Map<String, List<String>> map) {
//        Map<String, List<String>> rsMap = new TreeMap<>();
//        map.forEach(
//                (k, v) -> rsMap.put(k, new ArrayList<>(new TreeSet<>(v)))
//        );
//        return rsMap;
//    }
//
//    private void validateViewContext(String contextRegexp, Collection<String> contexts) {
//        Collection<String> contextSet = new TreeSet<>(
//                (Collection) ViewMgr.create(ViewMgr.VIEW_CONTEXT, contextRegexp, null, null, null)
//        );
//        assertEquals(
//                new TreeSet<>(contexts),
//                contextSet
//        );
//    }
//

}
