package test.utils;

import agent.base.plugin.PluginFactory;
import agent.base.utils.SystemConfig;
import agent.hook.plugin.ClassFinder;
import agent.jvmti.JvmtiUtils;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerTestUtils {
    private static final JvmtiUtils jvmtiUtils = JvmtiUtils.getInstance();

    static {
        jvmtiUtils.load(System.getProperty("user.dir") + "/../packaging/resources/server/native/libagent_jvmti_JvmtiUtils.so");
    }

    public static void initSystemConfig() throws Exception {
        SystemConfig.load(new File(System.getProperty("usr.dir"), "../packaging/resources/server/conf/server.conf").getAbsolutePath());
    }

    public static ClassFinder mockClassFinder() {
        return mockClassFinder(new TestClassLoader());
    }

    public static ClassFinder mockClassFinder(ClassLoader loader) {
        ClassFinder classFinder = mock(ClassFinder.class);
        PluginFactory.setMock(ClassFinder.class, classFinder);
        when(classFinder.findClassLoader(any())).thenReturn(loader);
        return classFinder;
    }

}
