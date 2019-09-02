package test.utils;

import agent.base.plugin.PluginFactory;
import agent.base.utils.SystemConfig;
import agent.hook.plugin.ClassFinder;

import java.io.File;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServerTestUtils {
    public static void initSystemConfig() throws Exception {
        SystemConfig.load(new File(System.getProperty("usr.dir"), "../packaging/resources/server/conf/server.conf").getAbsolutePath());
    }

    public static ClassFinder mockClassFinder() {
        ClassFinder classFinder = mock(ClassFinder.class);
        PluginFactory.setMock(ClassFinder.class, classFinder);
        when(classFinder.findClassLoader(any())).thenReturn(new TestClassLoader());
        return classFinder;
    }

}
