package agent.server.transform.impl.dynamic.rule;

import agent.server.transform.config.parser.handler.TreeRule;
import agent.server.transform.impl.dynamic.MethodInfo;
import agent.server.transform.impl.dynamic.SubTypeSearcher;

import java.util.Map;
import java.util.stream.Collectors;


public class ConfigurableTreeRule implements TreeRule {
    private final String context;
    private final String className;
    private final String method;
    private int maxLevel = 50;
    private String outputPath = null;
    private final TraverseRule traverseRule;

    public ConfigurableTreeRule(String context, String className, String method, TraverseRule traverseRule) {
        if (traverseRule == null)
            throw new IllegalArgumentException("Traverse rule can not be null.");
        this.context = context;
        this.className = className;
        this.method = method;
        this.traverseRule = traverseRule;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public String getTargetClass() {
        return className;
    }

    @Override
    public String getTargetMethod() {
        return method;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public TraverseRule getTraverseRule() {
        return traverseRule;
    }

    @Override
    public boolean accept(MethodInfo methodInfo) {
        return doFilter(methodInfo.className);
    }

    @Override
    public boolean stepInto(MethodInfo methodInfo) {
        return doFilter(methodInfo.className);
    }

    @Override
    public Map<String, Class<?>> getImplClasses(MethodInfo methodInfo, SubTypeSearcher subClassSearcher) {
        return subClassSearcher.get().entrySet().stream()
                .filter(entry -> doFilter(entry.getKey()))
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        )
                );
    }

    protected boolean doFilter(String className) {
        return true;
    }

//    @Override
//    protected void printTree(Tree<TimeData> tree) {
//        if (STDOUT.equals(outputPath)) {
//            printTree(System.out, tree);
//        } else {
//            try (PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputPath)))) {
//                printTree(out, tree);
//            } catch (Exception e) {
//                logger.error("print tree failed.", e);
//            }
//        }
//    }
}
