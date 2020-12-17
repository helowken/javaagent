package test.jvmti;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JNIDemoJava {

    static {
        System.load("/home/helowken/test_jni/debug_native/dist/libJNIDemo.so");
    }

    private static final int REPETITIONS = 100_000_000;

    private native int nativeCrash();

    private native int nativePrint();

    private native int nativeSleep(int ms);

    private native Double[] nativeAllocate(int n);

    private native void detectMethods();

    public static void main(String[] args) throws Exception {
//        System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2020-07-09 00:05:42.827").getTime());
//        String ALT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
//        SimpleDateFormat sdf = new SimpleDateFormat(ALT_DATE_TIME_FORMAT);
//        String[] ss = {
//                "2020-07-09 05:23:06.139",
//                "2020-07-09 05:23:06.145"
//        };
//        final long t = 8 * 3600 * 1000;
//        for (String s : ss) {
//            Date date = sdf.parse(s);
//            System.out.println(date.getTime() + t);
//        }

        JNIDemoJava nativeCall = new JNIDemoJava();
        nativeCall.nativePrint();

//        test3();
//        test1();
    }

    private static void test1() {
        JNIDemoJava nativeCall = new JNIDemoJava();
        nativeCall.nativePrint();
        nativeCall.nativeSleep(1000);
        Double[] dArr = nativeCall.nativeAllocate(10);
        for (Double d : dArr) {
            System.out.println(System.currentTimeMillis());
        }
    }

    private static void test2() {
        new JNIDemoJava().nativeCrash();
    }

    private static void test3() {
        new JNIDemoJava().detectMethods();
    }
}
