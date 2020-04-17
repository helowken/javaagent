package test.server.asm;

import agent.base.utils.IOUtils;
import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.tools.asm.*;
import test.server.TestClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class AsmTestUtils {

    static void doCheck(int count, List<String> logList, boolean throwError) {
        assertEquals(
                newExpectedList(count, throwError),
                logList
        );
    }

    private static List<String> newExpectedList(int count, boolean throwError) {
        List<String> prefixList = new ArrayList<>();
        prefixList.add("before");
        prefixList.add(throwError ? "onThrowing" : "onReturning");
        prefixList.add("after");

        List<String> expectedList = new ArrayList<>();
        for (String prefix : prefixList) {
            for (int i = 0; i < count; ++i) {
                expectedList.add(prefix + "-" + i);
            }
        }
        return expectedList;
    }

    static Class<?> prepareClassMethod(int count, List<String> logList, Class<?> clazz, String methodName) throws Exception {
        Method destMethod = ReflectionUtils.findFirstMethod(clazz, methodName);
        ProxyRegInfo regInfo = new ProxyRegInfo(destMethod);
        return prepareClass(count, logList, regInfo);
    }

    static Class<?> prepareClassConstructor(int count, List<String> logList, Class<?> clazz, String desc) throws Exception {
        Constructor constructor = ReflectionUtils.findConstructor(clazz, desc);
        ProxyRegInfo regInfo = new ProxyRegInfo(constructor);
        return prepareClass(count, logList, regInfo);
    }

    static Class<?> prepareClass(int count, List<String> logList, ProxyRegInfo regInfo) throws Exception {
        DestInvokeIdRegistry.getInstance().reg(
                regInfo.getDestInvoke()
        );
        ProxyResult item = prepareData(count, logList, regInfo);
        if (item.hasError())
            item.getError().printStackTrace();
        assertFalse(item.hasError());
        ProxyTransformMgr.getInstance().reg(
                Collections.singleton(item)
        );
        ProxyTransformMgr.getInstance().reg(
                Collections.singleton(item)
        );
        return AsmTestUtils.newClass(
                item.getTargetClass().getName(),
                item.getClassData()
        );
    }

    public static Class<?> newClass(String className, byte[] classData) {
        AsmUtils.verifyAndPrintResult(
                AsmTestUtils.class.getClassLoader(),
                classData,
                System.out
        );
        System.out.println("=========================\n");

        AsmUtils.print(classData, System.out);
        System.out.println("=========================\n");

        return new TestClassLoader().loadClass(className, classData);
    }

    static ProxyResult transform(ProxyRegInfo regInfo) {
        List<ProxyResult> items = ProxyTransformMgr.getInstance().transform(
                Collections.singleton(regInfo),
                clazz -> Utils.wrapToRtError(
                        () -> IOUtils.readBytes(
                                ClassLoader.getSystemResourceAsStream(clazz.getName().replace('.', '/') + ".class")
                        )
                )
        );
        assertEquals(1, items.size());
        return items.get(0);
    }

    static ProxyResult prepareData(int count, List<String> logList, ProxyRegInfo regInfo) throws Exception {
        for (int i = 0; i < count; ++i) {
            TestProxyB b = new TestProxyB(i, logList);
            String tag = "b" + i;
            regInfo.addBefore(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(TestProxyB.class, "testBefore"),
                            DEFAULT_BEFORE,
                            null,
                            tag
                    )
            ).addOnReturning(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(TestProxyB.class, "testOnReturning"),
                            DEFAULT_ON_RETURNING,
                            null,
                            tag
                    )
            ).addOnThrowing(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(TestProxyB.class, "testOnThrowing"),
                            DEFAULT_ON_THROWING,
                            null,
                            tag
                    )
            ).addAfter(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(TestProxyB.class, "testAfter"),
                            DEFAULT_AFTER,
                            null,
                            tag
                    )
            );
        }
        return transform(regInfo);
    }

    public static String methodToString(Method method) {
        return InvokeDescriptorUtils.descToText(
                method.getName() + InvokeDescriptorUtils.getDescriptor(method)
        );
    }

    public static String constructorToString(Constructor constructor) {
        return InvokeDescriptorUtils.descToText(
                "<init>" + InvokeDescriptorUtils.getDescriptor(constructor)
        );
    }
}
