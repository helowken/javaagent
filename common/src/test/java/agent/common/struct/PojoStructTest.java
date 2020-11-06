package agent.common.struct;

import agent.common.buffer.BufferAllocator;
import agent.common.struct.impl.*;
import agent.common.struct.impl.annotation.PojoClass;
import agent.common.struct.impl.annotation.PojoProperty;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static utils.TestUtils.checkEquals;
import static utils.TestUtils.isEquals;


public class PojoStructTest {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Random random = new Random();

    @Test
    public void test() {
        A a = new A();
        populateA(a);
        check(a);
    }

    @Test
    public void test2() {
        CA ca = new CA();
        populateCA(ca);
        check(ca);
    }

    @Test
    public void test3() {
        CCA cca = new CCA();
        populateCCA(cca);
        check(cca);
        measureTime(cca);
    }

    private B newB() {
        A a = new A();
        CA ca = new CA();
        CCA cca = new CCA();
        populateA(a);
        populateCA(ca);
        populateCCA(cca);
        B b = new B();
        b.setA(a);
        b.setCa(ca);
        b.setCca(cca);
        b.setB1("b1");
        b.setB2(true);
        return b;
    }

    @Test
    public void test4() {
        check(newB());
    }

    private Collection<A> newAs() {
        List<A> as = new ArrayList<>();
        for (int i = 0; i < 5; ++i) {
            A a = new A();
            populateA(a);
            as.add(a);
        }
        return as;
    }

    @Test
    public void test5() {
        check(newB2());
    }

    @Test
    public void testMeasure() {
        measureTime(newB2());
    }

    @Test
    public void testTypeConvert() {
        B3 b = new B3();
        A a = new A();
        populateA(a);
        b.setValue(a);
        check(b);

        CA ca = new CA();
        populateCA(ca);
        b.setValue(ca);
        check(b);

        CCA cca = new CCA();
        populateCCA(cca);
        b.setValue(cca);
        check(b);
    }

    @Test
    public void testWildcardType() {
        B4 b = new B4();
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 3; ++i) {
            map.put("k-" + i, "v-" + i);
        }
        b.setA(map);
        b.setA2(map);
        b.setA3(map);
        Set<Integer> set = new HashSet<>(Arrays.asList(1, 2, 3, 4, 5));
        b.setB(set);
        b.setB2(set);
        List<Float> list = Arrays.asList(1.1F, 2.2F, 3.3F);
        b.setC(list);
        b.setC2(list);
        check(b);
    }

    @Test
    public void testWildcardType2() {
        B4 b = new B4();
        Map<String, A> map = new HashMap<>();
        Map<CA, String> map2 = new HashMap<>();
        Map<A, CCA> map3 = new HashMap<>();
        List<CCA> list = new ArrayList<>();
        List<CA> list2 = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            A a = new A();
            populateA(a);
            CA ca = new CA();
            populateCA(ca);
            CCA cca = new CCA();
            populateCCA(cca);

            map.put("k-" + i, a);
            map2.put(ca, "v-" + i);
            map3.put(a, cca);
            list.add(cca);
            list2.add(ca);
        }
        b.setA(map3);
        b.setA2(map);
        b.setA3(map2);

        b.setB(new HashSet<>(new HashSet<>(list)));
        b.setB2(new HashSet<>(list2));
        b.setC(list);
        b.setC2(list2);
        check(b);
    }

    @Test
    public void test6() {
        final int threadType = 1;
        final int stElementType = 2;
        StructContext context = new StructContext();
        context.addPojoInfo(
                Thread.class::isAssignableFrom,
                new PojoInfo<>(
                        threadType,
                        null,
                        new PojoFieldPropertyList<>(
                                new PojoFieldProperty<>(Long.class, 0, null, Thread::getId),
                                new PojoFieldProperty<>(String.class, 1, null, Thread::getName)
                        )
                )
        ).addPojoInfo(
                StackTraceElement.class,
                new PojoInfo<>(
                        stElementType,
                        null,
                        new PojoFieldPropertyList<>(
                                new PojoFieldProperty<>(String.class, 0, null, StackTraceElement::getClassName),
                                new PojoFieldProperty<>(String.class, 1, null, StackTraceElement::getMethodName)
                        )
                )
        );

        Map<Thread, StackTraceElement[]> m = Thread.getAllStackTraces();
        Map<StEntity, StElement[]> stMap = new HashMap<>();
        m.forEach(
                (t, els) -> {
                    StEntity entity = new StEntity();
                    entity.setThreadId(t.getId());
                    entity.setThreadName(t.getName());
                    List<StElement> elements = new ArrayList<>();
                    for (StackTraceElement el : els) {
                        StElement element = new StElement();
                        element.setClazz(el.getClassName());
                        element.setMethod(el.getMethodName());
                        elements.add(element);
                    }
                    stMap.put(entity, elements.toArray(new StElement[0]));
                }
        );
        ByteBuffer bb = Struct.serialize(m, context);
        bb.flip();

        context.clear();
        context.setPojoCreator(
                type -> {
                    switch (type) {
                        case threadType:
                            return new StEntity();
                        case stElementType:
                            return new StElement();
                        default:
                            return null;
                    }
                }
        );
        Map<Object, Object[]> map = Struct.deserialize(bb, context);
        map.forEach(
                (key, value) -> System.out.println(key + " = " + Arrays.toString(value))
        );
        checkEquals(stMap, map);
    }

    @Test
    public void test7() {
        List<A> v1 = new ArrayList<>();
        Map<B, A> v2 = new HashMap<>();
        List<Set<A>> v3 = new LinkedList<>();
        Collection<A[]> v4 = new HashSet<>();
        for (int i = 0; i < 5; ++i) {
            CCA cca = new CCA();
            populateCCA(cca);
            v1.add(cca);

            B b = newB();
            v2.put(b, cca);

            Set<A> vs = new HashSet<>();
            for (int j = 0; j < 3; ++j) {
                CA ca = new CA();
                populateCA(ca);
                vs.add(ca);
            }
            v3.add(vs);
            v4.add(vs.toArray(new A[0]));
        }
//        check(v1);
//        check(v2);
//        check(v3);
//        check(v4);

    }

    @Test
    public void test8() {
        C c = new C();
        List<A[]> aList = new ArrayList<>();
        List<Collection<A[]>> aListList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            aList.add(newAArray());

            List<A[]> list = new ArrayList<>();
            for (int j = 0; j < 4; ++j) {
                list.add(newAArray());
            }
            aListList.add(list);
        }
        c.setA(aList);
        c.setB(aListList);
        check(c);
    }

    private A[] newAArray() {
        A[] as = new A[3];
        for (int i = 0; i < as.length; ++i) {
            A a = new A();
            populateA(a);
            as[i] = a;
        }
        return as;
    }

    private B2 newB2() {
        B2 b2 = new B2();
        b2.setAList(new ArrayList<>(newAs()));
        b2.setAColl(new ArrayList<>(newAs()));
        b2.setASet(new HashSet<>(newAs()));
        b2.setAs(newAs().toArray(new A[0]));
        Map<String, A> aMap = new HashMap<>();
        for (int i = 0; i < 3; ++i) {
            A a = new A();
            populateA(a);
            aMap.put("k-" + i, a);
        }
        b2.setAMap(aMap);

        Map<A, CA> aca = new HashMap<>();
        for (int i = 0; i < 3; ++i) {
            A a = new A();
            populateA(a);
            CA ca = new CA();
            populateCA(ca);
            aca.put(a, ca);
        }
        b2.setAca(aca);

        Map<A[], Integer> arrayToInt = new HashMap<>();
        for (int i = 0; i < 3; ++i) {
            A[] array = new A[4];
            for (int j = 0; j < 4; ++j) {
                A a = new A();
                populateA(a);
                array[j] = a;
            }
            arrayToInt.put(array, i);
        }
        b2.setArrayToInt(arrayToInt);

        LinkedList<B[]> bList = new LinkedList<>();
        for (int i = 0; i < 4; ++i) {
            B[] bs = new B[3];
            for (int j = 0; j < bs.length; ++j) {
                bs[j] = newB();
            }
            bList.add(bs);
        }
        b2.setBs(bList);

        TreeMap<String, Boolean> treeMap = new TreeMap<>();
        for (int i = 0; i < 5; ++i) {
            treeMap.put("ttt-" + i, i % 2 == 0);
        }
        b2.setTreeMap(treeMap);
        return b2;
    }

    private void measureTime(Object o) {
        final int count = 200;
        useJson(o, count);
        System.out.println("===========================\n");
        useJson2(o, count);
        System.out.println("===========================\n");
        useStruct(o, count);
    }

    private void useStruct(Object o, final int count) {
        StructContext context = new StructContext();
        ByteBuffer bb = BufferAllocator.allocate(
                Struct.bytesSize(o, context)
        );
        BBuff buff = new DefaultBBuff(bb);
        calculate(
                () -> Struct.serialize(buff, o, context),
                () -> {
                    System.out.println("======: " + bb.position());
                    bb.flip();
                    context.clearCache();
                },
                count,
                "Struct"
        );
    }

    private void useJson(Object o, final int count) {
        calculate(
                () -> {
                    try {
                        String s = objectMapper.writeValueAsString(o);
                        System.out.println("####: " + s.getBytes().length);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                null,
                count,
                "Jason (no expand key) "
        );
    }

    private void useJson2(Object o, final int count) {
        calculate(
                () -> {
                    try {
                        String s = JSONObject.toJSONString(o);
                        System.out.println("======: " + s.getBytes().length);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                null,
                count,
                "Fastjson "
        );
    }

    private void calculate(Runnable runnable, Runnable postFunc, final int count, String msg) {
        long total = 0;
        for (int i = 0; i < count; ++i) {
            long st = System.nanoTime();
            runnable.run();
            long et = System.nanoTime();
            total += et - st;
            if (postFunc != null)
                postFunc.run();
        }
        System.out.println(msg + " Avg: " + ((float) total / count / 1000000));
    }

    private void check(Object o) {
        StructContext context = new StructContext();
        ByteBuffer bb = Struct.serialize(o, context);

        bb.flip();
        Object o2 = Struct.deserialize(bb, context);
        if (o2.getClass().isArray())
            System.out.println(Arrays.toString((Object[]) o2));
        else
            System.out.println(o2);
        checkEquals(o, o2);
    }

    private void populateA(A a) {
        a.setA(random.nextInt());
        a.setB(true);
        a.setC(UUID.randomUUID().toString());
        a.setD(random.nextFloat());
        a.setEs(Arrays.asList(1.1f, 2.2f, 3.3f));
        a.setFs(new HashSet<>(Arrays.asList(4L, 5L, 6L, 7L)));
        a.setGs(Arrays.asList("as", "bs", "cs", "ds", "es"));
        a.setHs(new int[]{10, 11, 12, 13, 14});
        Map<Short, Boolean> js = new TreeMap<>();
        for (short i = 20; i < 25; ++i) {
            js.put(i, i % 2 == 0);
        }
        a.setJs(js);
    }

    private void populateCA(CA ca) {
        populateA(ca);
        ca.setA(random.nextInt());
        ca.setE(random.nextDouble());
        ca.setF(random.nextLong());
    }

    private void populateCCA(CCA cca) {
        populateCA(cca);
        cca.setG((short) random.nextInt());
        cca.setH((byte) random.nextInt());
    }

    @PojoClass(type = 1)
    public static class A {
        @PojoProperty(index = 0)
        private int a;
        @PojoProperty(index = 1)
        private boolean b;
        @PojoProperty(index = 2)
        private String c;
        @PojoProperty(index = 3)
        private float d;
        @PojoProperty(index = 4)
        private Collection<Float> es;
        @PojoProperty(index = 5)
        private Set<Long> fs;
        @PojoProperty(index = 6)
        private List<String> gs;
        @PojoProperty(index = 7)
        private int[] hs;
        @PojoProperty(index = 8)
        private Map<Short, Boolean> js;

        public Collection<Float> getEs() {
            return es;
        }

        public void setEs(Collection<Float> es) {
            this.es = es;
        }

        public Set<Long> getFs() {
            return fs;
        }

        public void setFs(Set<Long> fs) {
            this.fs = fs;
        }

        public List<String> getGs() {
            return gs;
        }

        public void setGs(List<String> gs) {
            this.gs = gs;
        }

        public int[] getHs() {
            return hs;
        }

        public void setHs(int[] hs) {
            this.hs = hs;
        }

        public Map<Short, Boolean> getJs() {
            return js;
        }

        public void setJs(Map<Short, Boolean> js) {
            this.js = js;
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public boolean isB() {
            return b;
        }

        public void setB(boolean b) {
            this.b = b;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public float getD() {
            return d;
        }

        public void setD(float d) {
            this.d = d;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            A a1 = (A) o;
            return a == a1.a &&
                    b == a1.b &&
                    Float.compare(a1.d, d) == 0 &&
                    Objects.equals(c, a1.c) &&
                    Objects.equals(es, a1.es) &&
                    Objects.equals(fs, a1.fs) &&
                    Objects.equals(gs, a1.gs) &&
                    Arrays.equals(hs, a1.hs) &&
                    Objects.equals(js, a1.js);
        }

        @Override
        public int hashCode() {

            int result = Objects.hash(a, b, c, d, es, fs, gs, js);
            result = 31 * result + Arrays.hashCode(hs);
            return result;
        }

        @Override
        public String toString() {
            return "A{" +
                    "a=" + a +
                    ", b=" + b +
                    ", c='" + c + '\'' +
                    ", d=" + d +
                    ", es=" + es +
                    ", fs=" + fs +
                    ", gs=" + gs +
                    ", hs=" + Arrays.toString(hs) +
                    ", js=" + js +
                    '}';
        }
    }

    @PojoClass(type = 2)
    public static class CA extends A {
        @PojoProperty(index = 0)
        private int a;
        @PojoProperty(index = 1)
        private double e;
        @PojoProperty(index = 2)
        private long f;

        @Override
        public int getA() {
            return a;
        }

        @Override
        public void setA(int a) {
            this.a = a;
        }

        public double getE() {
            return e;
        }

        public void setE(double e) {
            this.e = e;
        }

        public long getF() {
            return f;
        }

        public void setF(long f) {
            this.f = f;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            CA ca = (CA) o;
            return a == ca.a &&
                    Double.compare(ca.e, e) == 0 &&
                    f == ca.f;
        }

        @Override
        public int hashCode() {

            return Objects.hash(super.hashCode(), a, e, f);
        }

        @Override
        public String toString() {
            return "CA{" +
                    "a=" + a +
                    ", e=" + e +
                    ", f=" + f +
                    ", " + super.toString() +
                    '}';
        }
    }

    @PojoClass(type = 3)
    public static class CCA extends CA {
        @PojoProperty(index = -1)
        private short g;
        @PojoProperty(index = -2)
        private byte h;

        public short getG() {
            return g;
        }

        public void setG(short g) {
            this.g = g;
        }

        public byte getH() {
            return h;
        }

        public void setH(byte h) {
            this.h = h;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            CCA cca = (CCA) o;
            return g == cca.g &&
                    h == cca.h;
        }

        @Override
        public int hashCode() {

            return Objects.hash(super.hashCode(), g, h);
        }

        @Override
        public String toString() {
            return "CCA{" +
                    "g=" + g +
                    ", h=" + h +
                    ", " + super.toString() +
                    '}';
        }
    }

    @PojoClass(type = 4)
    public static class B {
        @PojoProperty(index = 100)
        private A a;
        @PojoProperty(index = 200)
        private CA ca;
        @PojoProperty(index = 300)
        private CCA cca;
        @PojoProperty(index = 400)
        private String b1;
        @PojoProperty(index = 500)
        private boolean b2;

        public A getA() {
            return a;
        }

        public void setA(A a) {
            this.a = a;
        }

        public CA getCa() {
            return ca;
        }

        public void setCa(CA ca) {
            this.ca = ca;
        }

        public CCA getCca() {
            return cca;
        }

        public void setCca(CCA cca) {
            this.cca = cca;
        }

        public String getB1() {
            return b1;
        }

        public void setB1(String b1) {
            this.b1 = b1;
        }

        public boolean isB2() {
            return b2;
        }

        public void setB2(boolean b2) {
            this.b2 = b2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            B b = (B) o;
            return b2 == b.b2 &&
                    Objects.equals(a, b.a) &&
                    Objects.equals(ca, b.ca) &&
                    Objects.equals(cca, b.cca) &&
                    Objects.equals(b1, b.b1);
        }

        @Override
        public int hashCode() {

            return Objects.hash(a, ca, cca, b1, b2);
        }

        @Override
        public String toString() {
            return "B{" +
                    "a=" + a +
                    ", ca=" + ca +
                    ", cca=" + cca +
                    ", b1='" + b1 + '\'' +
                    ", b2=" + b2 +
                    '}';
        }
    }

    @PojoClass(type = 5)
    public static class B2 {
        @PojoProperty(index = 0)
        private List<A> aList;
        @PojoProperty(index = 1)
        private Map<String, A> aMap;
        @PojoProperty(index = 2)
        private Set<A> aSet;
        @PojoProperty(index = 3)
        private Collection<A> aColl;
        @PojoProperty(index = 4)
        private A[] as;
        @PojoProperty(index = 5)
        private Map<A, CA> aca;

        public TreeMap<String, Boolean> getTreeMap() {
            return treeMap;
        }

        public void setTreeMap(TreeMap<String, Boolean> treeMap) {
            this.treeMap = treeMap;
        }

        @PojoProperty(index = 6)
        private Map<A[], Integer> arrayToInt;
        @PojoProperty(index = 7)
        private LinkedList<B[]> bs;
        @PojoProperty(index = 8)
        private TreeMap<String, Boolean> treeMap;

        public LinkedList<B[]> getBs() {
            return bs;
        }

        public void setBs(LinkedList<B[]> bs) {
            this.bs = bs;
        }

        public Map<A[], Integer> getArrayToInt() {
            return arrayToInt;
        }

        public void setArrayToInt(Map<A[], Integer> arrayToInt) {
            this.arrayToInt = arrayToInt;
        }

        public Map<A, CA> getAca() {
            return aca;
        }

        public void setAca(Map<A, CA> aca) {
            this.aca = aca;
        }

        public List<A> getAList() {
            return aList;
        }

        public void setAList(List<A> aList) {
            this.aList = aList;
        }

        public Map<String, A> getAMap() {
            return aMap;
        }

        public void setAMap(Map<String, A> aMap) {
            this.aMap = aMap;
        }

        public Set<A> getASet() {
            return aSet;
        }

        public void setASet(Set<A> aSet) {
            this.aSet = aSet;
        }

        public Collection<A> getAColl() {
            return aColl;
        }

        public void setAColl(Collection<A> aColl) {
            this.aColl = aColl;
        }

        public A[] getAs() {
            return as;
        }

        public void setAs(A[] as) {
            this.as = as;
        }

        @Override
        public String toString() {
            return "B2{" +
                    "aList=" + aList +
                    ", aMap=" + aMap +
                    ", aSet=" + aSet +
                    ", aColl=" + aColl +
                    ", as=" + Arrays.toString(as) +
                    ", aca=" + aca +
                    ", arrayToInt=" + arrayToInt +
                    ", bs=" + bs +
                    ", treeMap=" + treeMap +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            B2 b2 = (B2) o;
            boolean v = Objects.equals(aList, b2.aList) &&
                    Objects.equals(aMap, b2.aMap) &&
                    Objects.equals(aSet, b2.aSet) &&
                    Objects.equals(aColl, b2.aColl) &&
                    Arrays.equals(as, b2.as) &&
                    Objects.equals(aca, b2.aca) &&
                    Objects.equals(treeMap, b2.treeMap);
            return v &&
                    isMapEquals(b2) &&
                    isListEquals(b2);
        }

        private boolean isListEquals(B2 b2) {
            if (bs == b2.bs)
                return true;
            if (bs == null || b2.bs == null)
                return false;
            if (bs.size() != b2.bs.size())
                return false;
            for (int i = 0, len = bs.size(); i < len; ++i) {
                if (!Arrays.equals(bs.get(i), b2.bs.get(i)))
                    return false;
            }
            return true;
        }

        private boolean isMapEquals(B2 b2) {
            if (arrayToInt == b2.arrayToInt)
                return true;
            if (arrayToInt == null || b2.arrayToInt == null)
                return false;
            if (arrayToInt.size() != b2.arrayToInt.size())
                return false;
            for (Map.Entry<A[], Integer> entry : arrayToInt.entrySet()) {
                boolean keyMatch = false;
                for (Map.Entry<A[], Integer> entry2 : b2.arrayToInt.entrySet()) {
                    if (Arrays.equals(entry.getKey(), entry2.getKey())) {
                        keyMatch = true;
                        if (!Objects.equals(entry.getValue(), entry2.getValue()))
                            return false;
                    }
                }
                if (!keyMatch)
                    return false;
            }
            return true;
        }

        @Override
        public int hashCode() {

            int result = Objects.hash(aList, aMap, aSet, aColl, aca, treeMap);
            result = 31 * result + Arrays.hashCode(as);
            return result;
        }
    }

    @PojoClass(type = 6)
    public static class B3 {
        @PojoProperty(index = 0)
        Object value;

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            B3 b3 = (B3) o;
            return Objects.equals(value, b3.value);
        }

        @Override
        public int hashCode() {

            return Objects.hash(value);
        }

        @Override
        public String toString() {
            return "B3{" +
                    "value=" + value +
                    '}';
        }
    }

    @PojoClass(type = 7)
    public static class B4 {
        @PojoProperty(index = 0)
        private Map a;
        @PojoProperty(index = 1)
        private Map<String, ?> a2;
        @PojoProperty(index = 2)
        private Map<?, String> a3;
        @PojoProperty(index = 3)
        private Set b;
        @PojoProperty(index = 4)
        private Set<?> b2;
        @PojoProperty(index = 5)
        private List c;
        @PojoProperty(index = 6)
        private List<?> c2;

        public Map getA() {
            return a;
        }

        public void setA(Map a) {
            this.a = a;
        }

        public Map<String, ?> getA2() {
            return a2;
        }

        public void setA2(Map<String, ?> a2) {
            this.a2 = a2;
        }

        public Map<?, String> getA3() {
            return a3;
        }

        public void setA3(Map<?, String> a3) {
            this.a3 = a3;
        }

        public Set getB() {
            return b;
        }

        public void setB(Set b) {
            this.b = b;
        }

        public Set<?> getB2() {
            return b2;
        }

        public void setB2(Set<?> b2) {
            this.b2 = b2;
        }

        public List getC() {
            return c;
        }

        public void setC(List c) {
            this.c = c;
        }

        public List<?> getC2() {
            return c2;
        }

        public void setC2(List<?> c2) {
            this.c2 = c2;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            B4 b4 = (B4) o;
            return Objects.equals(a, b4.a) &&
                    Objects.equals(a2, b4.a2) &&
                    Objects.equals(a3, b4.a3) &&
                    Objects.equals(b, b4.b) &&
                    Objects.equals(b2, b4.b2) &&
                    Objects.equals(c, b4.c) &&
                    Objects.equals(c2, b4.c2);
        }

        @Override
        public int hashCode() {

            return Objects.hash(a, a2, a3, b, b2, c, c2);
        }

        @Override
        public String toString() {
            return "B4{" +
                    "a=" + a +
                    ", a2=" + a2 +
                    ", a3=" + a3 +
                    ", b=" + b +
                    ", b2=" + b2 +
                    ", c=" + c +
                    ", c2=" + c2 +
                    '}';
        }
    }

    @PojoClass(type = 8)
    public static class B5<T, V extends String> {
        private T a;
        private V b;
        private Map<List<T>, T[]> c;
        private Map<?, Map<T, V>> d;
        private V[] e;
    }

    @PojoClass(type = 9)
    public static class StEntity {
        @PojoProperty(index = 0)
        private long threadId;
        @PojoProperty(index = 1)
        private String threadName;

        public long getThreadId() {
            return threadId;
        }

        public void setThreadId(long threadId) {
            this.threadId = threadId;
        }

        public String getThreadName() {
            return threadName;
        }

        public void setThreadName(String threadName) {
            this.threadName = threadName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StEntity stEntity = (StEntity) o;
            return threadId == stEntity.threadId &&
                    Objects.equals(threadName, stEntity.threadName);
        }

        @Override
        public int hashCode() {

            return Objects.hash(threadId, threadName);
        }

        @Override
        public String toString() {
            return "StEntity{" +
                    "threadId=" + threadId +
                    ", threadName='" + threadName + '\'' +
                    '}';
        }
    }

    @PojoClass(type = 10)
    public static class StElement {
        @PojoProperty(index = 0)
        private String clazz;
        @PojoProperty(index = 1)
        private String method;

        public String getClazz() {
            return clazz;
        }

        public void setClazz(String clazz) {
            this.clazz = clazz;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            StElement stElement = (StElement) o;
            return Objects.equals(clazz, stElement.clazz) &&
                    Objects.equals(method, stElement.method);
        }

        @Override
        public int hashCode() {

            return Objects.hash(clazz, method);
        }

        @Override
        public String toString() {
            return "StElement{" +
                    "clazz='" + clazz + '\'' +
                    ", method='" + method + '\'' +
                    '}';
        }
    }

    @PojoClass(type = 11)
    public static class C {
        @PojoProperty(index = 0)
        private List<A[]> a;
        @PojoProperty(index = 1)
        private List<Collection<A[]>> b;

        public List<A[]> getA() {
            return a;
        }

        public void setA(List<A[]> a) {
            this.a = a;
        }

        public List<Collection<A[]>> getB() {
            return b;
        }

        public void setB(List<Collection<A[]>> b) {
            this.b = b;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            C c = (C) o;
            return isEquals(a, c.a) &&
                    isEquals(b, c.b);
        }

        @Override
        public int hashCode() {

            return Objects.hash(a, b);
        }

        @Override
        public String toString() {
            return "C{" +
                    "a=" + a +
                    ", b=" + b +
                    '}';
        }
    }
}
