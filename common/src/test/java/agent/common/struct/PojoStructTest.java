package agent.common.struct;

import agent.common.buffer.BufferAllocator;
import agent.common.struct.impl.PojoStruct;
import agent.common.struct.impl.Structs;
import agent.common.utils.annotation.PojoProperty;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.assertEquals;


public class PojoStructTest {
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
    }

    @Test
    public void test4() {
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

        check(b);
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

        check(b2);
    }

    private void check(Object o) {
        PojoStruct struct = Structs.newPojo(o.getClass());
        struct.setPojo(o);

        ByteBuffer bb = BufferAllocator.allocate(
                struct.bytesSize()
        );
        BBuff buff = new DefaultBBuff(bb);
        struct.serialize(buff);

        bb.flip();
        struct.deserialize(buff);
        Object o2 = struct.getPojo();
        assertEquals(o, o2);
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
    }

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
                    '}';
        }
    }

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
                    '}';
        }
    }

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
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            B2 b2 = (B2) o;
            return Objects.equals(aList, b2.aList) &&
                    Objects.equals(aMap, b2.aMap) &&
                    Objects.equals(aSet, b2.aSet) &&
                    Objects.equals(aColl, b2.aColl) &&
                    Arrays.equals(as, b2.as) &&
                    Objects.equals(aca, b2.aca);
        }

        @Override
        public int hashCode() {

            int result = Objects.hash(aList, aMap, aSet, aColl, aca);
            result = 31 * result + Arrays.hashCode(as);
            return result;
        }
    }
}
