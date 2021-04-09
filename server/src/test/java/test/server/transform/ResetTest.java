package test.server.transform;

import agent.cmdline.command.DefaultCommand;
import agent.common.config.ClassFilterConfig;
import agent.common.config.InfoQuery;
import agent.common.config.ModuleConfig;
import agent.common.config.TargetConfig;
import agent.invoke.DestInvoke;
import agent.server.command.executor.ServerCmdExecMgr;
import agent.server.transform.TransformMgr;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.impl.InfoMgr;
import org.junit.Test;
import test.server.AbstractTest;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static agent.base.utils.AssertUtils.assertEquals;
import static agent.common.message.MessageType.CMD_SEARCH;
import static agent.common.message.MessageType.CMD_TRANSFORM;

public class ResetTest extends AbstractTest {
    @Test
    public void test() {
        byte[] bs = getClassData(A.class);
        String className = A.class.getName();
        loader.loadClass(className, bs);

        ModuleConfig moduleConfig = new ModuleConfig();
        TargetConfig targetConfig = new TargetConfig();

        ClassFilterConfig classFilterConfig = new ClassFilterConfig();
        classFilterConfig.setIncludes(
                Collections.singleton("*A")
        );
        targetConfig.setClassFilter(classFilterConfig);
        moduleConfig.setTargets(
                Collections.singletonList(targetConfig)
        );

        Map<String, Collection<String>> rsMap = ServerCmdExecMgr.exec(
                new DefaultCommand(CMD_SEARCH, moduleConfig)
        ).getContent();
        assertEquals(2, rsMap.size());

        rsMap.forEach(
                (k, v) -> System.out.println(k + " -> " + v)
        );
        System.out.println("=================");

        Set<DestInvoke> invokeSet = TransformMgr.getInstance().searchInvokes(moduleConfig);
        invokeSet.forEach(DestInvokeIdRegistry.getInstance()::reg);

        InfoQuery query = new InfoQuery();
        query.setTargetConfig(targetConfig);
        query.setLevel(InfoQuery.INFO_INVOKE);
        query.setWithCalls(false);
        rsMap = InfoMgr.create(query);
        assertEquals(2, rsMap.size());
        rsMap.forEach(
                (k, v) -> System.out.println(k + " -> " + v)
        );
    }

    public static class A {
        public void test() {
        }
    }
}
