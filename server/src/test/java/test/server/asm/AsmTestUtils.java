package test.server.asm;

import agent.base.utils.IOUtils;
import agent.base.utils.ReflectionUtils;
import agent.base.utils.Utils;
import agent.server.transform.tools.asm.*;
import test.server.utils.TestClassLoader;

import java.util.Collections;
import java.util.List;

import static agent.server.transform.tools.asm.ProxyArgsMask.*;
import static org.junit.Assert.assertEquals;

class AsmTestUtils {
    static Class<?> prepareClass(int count, List<String> logList, ProxyRegInfo regInfo) throws Exception {
        ProxyResult item = prepareData(count, logList, regInfo);
        ProxyTransformMgr.getInstance().reg(
                Collections.singleton(item)
        );
        return AsmTestUtils.newClass(
                item.getTargetClass().getName(),
                item.getClassData()
        );
    }

    static Class<?> newClass(String className, byte[] classData) {
        AsmUtils.verifyAndPrintResult(classData);
        System.out.println("=========================\n");

        AsmUtils.print(classData);
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
            TestProxyB b = new TestProxyB(logList);
            regInfo.addBefore(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(TestProxyB.class, "testBefore"),
                            DEFAULT_BEFORE
                    )
            ).addAfter(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(TestProxyB.class, "testAfter"),
                            DEFAULT_AFTER
                    )
            ).addAround(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(TestProxyB.class, "testAround"),
                            DEFAULT_AROUND
                    )
            ).addAfterReturning(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(TestProxyB.class, "testAfterReturning"),
                            DEFAULT_AFTER_RETURNING
                    )
            ).addAfterThrowing(
                    new ProxyCallInfo(
                            b,
                            ReflectionUtils.findFirstMethod(TestProxyB.class, "testAfterThrowing"),
                            DEFAULT_AFTER_THROWING
                    )
            );
        }
        return transform(regInfo);
    }
}
