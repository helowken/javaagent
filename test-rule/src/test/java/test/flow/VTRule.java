package test.flow;

import agent.server.transform.impl.dynamic.rule.ConfigurableTreeRule;

public class VTRule extends ConfigurableTreeRule {

    public VTRule() {
        super(
                "/oauth2-api",
                "com.ericsson.ngin.oauthserver.business.TokenExtractor",
                "getAccessTokenLite",
//                "/home/jeeapp/tree-time.txt",
                null
        );
    }

    @Override
    protected boolean doFilter(String className) {
        String upper = className.toUpperCase();
        return className.startsWith("com.ericsson.") && !upper.contains("tag") && !upper.contains("log");
    }

//    @Override
//    protected void printNode(PrintStream outputStream, RuleNode<TimeData> node) {
//        TimeData timeData = node.getData();
//        if (timeData.endTime - timeData.startTime > 0)
//            super.printNode(outputStream, node);
//    }
}
