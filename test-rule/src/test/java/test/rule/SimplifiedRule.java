package test.rule;

import agent.server.transform.impl.dynamic.rule.ConfigurableTreeRule;
import agent.server.transform.impl.dynamic.rule.TreeTimeMeasureRule;

public class SimplifiedRule extends ConfigurableTreeRule {
    public SimplifiedRule() {
        super(
                "/test",
                "test.aop.AAA",
                "run",
//                "/home/jeeapp/tree-time.txt",
                new TreeTimeMeasureRule()
        );
    }
}
