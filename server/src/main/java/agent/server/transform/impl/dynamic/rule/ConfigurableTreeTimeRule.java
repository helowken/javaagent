package agent.server.transform.impl.dynamic.rule;

import agent.base.utils.Logger;
import agent.server.transform.config.parser.handler.TreeTimeRuleConfig;
import agent.server.transform.impl.dynamic.MethodInfo;
import agent.server.transform.impl.dynamic.SubTypeSearcher;
import agent.server.tree.Tree;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.stream.Collectors;

import static agent.server.utils.log.LogConfig.STDOUT;

public class ConfigurableTreeTimeRule extends TreeTimeMeasureRule implements TreeTimeRuleConfig {
    private final Logger logger;
    private final String context;
    private final String className;
    private final String method;
    private final int maxLevel;
    private final String outputPath;

    public ConfigurableTreeTimeRule(String context, String className, String method, String outputPath) {
        this(context, className, method, 0, outputPath);
    }

    public ConfigurableTreeTimeRule(String context, String className, String method, int maxLevel, String outputPath) {
        this.context = context;
        this.className = className;
        this.method = method;
        this.maxLevel = maxLevel;
        this.outputPath = outputPath == null ? STDOUT : outputPath;
        this.logger = Logger.getLogger(getClass());
    }

    @Override
    public String getContext() {
        return context;
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getMethod() {
        return method;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
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

    @Override
    protected void printTree(Tree<TimeData> tree) {
        if (STDOUT.equals(outputPath)) {
            printTree(System.out, tree);
        } else {
            try (PrintStream out = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputPath)))) {
                printTree(out, tree);
            } catch (Exception e) {
                logger.error("print tree failed.", e);
            }
        }
    }
}
