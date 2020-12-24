package test.server.search;

import agent.common.args.parse.FilterItem;
import agent.common.args.parse.FilterOptUtils;
import agent.common.config.ClassFilterConfig;
import agent.common.config.MethodFilterConfig;
import agent.common.config.TargetConfig;
import agent.invoke.DestInvoke;
import agent.invoke.MethodInvoke;
import agent.server.transform.search.filter.ClassFilter;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.InvokeFilter;
import agent.server.transform.tools.asm.AsmUtils;
import org.junit.Test;
import test.server.AbstractTest;

import java.lang.reflect.Method;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FilterUtilsTest extends AbstractTest {
    @Test
    public void test() {
        new TestObject();
        TargetConfig targetConfig = FilterOptUtils.createTargetConfig(
                new FilterItem(
                        "*.TestObject",
                        "lambda$test$0",
                        null
                )
        );
        ClassFilterConfig classFilterConfig = targetConfig.getClassFilter();
        MethodFilterConfig methodFilterConfig = targetConfig.getMethodFilter();
        ClassFilter classFilter = FilterUtils.newClassFilter(classFilterConfig, false);
        InvokeFilter methodFilter = FilterUtils.newInvokeFilter(methodFilterConfig);
        assertTrue(
                classFilter.accept(TestObject.class)
        );
        for (Method method : TestObject.class.getDeclaredMethods()) {
            if (method.getName().startsWith("lambda$")) {
                System.out.println("==========: " + method);
                DestInvoke invoke = new MethodInvoke(method);
                assertTrue(
                        methodFilter.accept(
                                AsmUtils.getInvokeFullName(
                                        invoke.getName(),
                                        invoke.getDescriptor()
                                )
                        )
                );
                return;
            }
        }
        fail("No method found.");
    }
}
