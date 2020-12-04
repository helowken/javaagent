package agent.common.tree;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class TreeUtils {
    //    private static final String branch = "├─";
//    private static final String branch2 = "│   ";
//    private static final String end = "└─";
//    private static final String indent = "    ";
    private static final String branch = "+---";
    private static final String branch2 = "|   ";
    private static final String end = "`---";
    private static final String indent = "    ";
    private static final PrintConfig defaultConfig = new PrintConfig(true);

    public static <T> void traverse(Node<T> tree, Consumer<Node<T>> nodeAccessor) {
        traverse(tree, nodeAccessor, null);
    }

    public static <T> void traverse(Node<T> tree, Consumer<Node<T>> nodeAccessor, Predicate<Node<T>> searchChildrenPredicate) {
        LinkedList<Node<T>> restNodes = new LinkedList<>();
        restNodes.add(tree);
        while (!restNodes.isEmpty()) {
            Node<T> node = restNodes.pop();
            nodeAccessor.accept(node);
            if (searchChildrenPredicate == null || searchChildrenPredicate.test(node))
                restNodes.addAll(0, node.getChildren());
        }
    }

    public static <O extends OutputStream, T> void printTree(O outputStream, Node<T> tree, PrintConfig config, NodePrinter<O, T> nodePrinter) {
        traverse(
                tree,
                node -> nodePrinter.print(
                        outputStream,
                        node,
                        config == null ? defaultConfig : config
                )
        );
    }

    public static <O extends PrintStream, T> void printTree(O outputStream, Node<T> tree, PrintConfig config,
                                                            BiFunction<Node<T>, PrintConfig, String> nodeToStringFunc) {
        printTree(
                outputStream,
                tree,
                config,
                (out, node, printConfig) -> {
                    if (!node.isRoot() || printConfig.rootVisible) {
                        List<String> rows = splitContent(
                                nodeToStringFunc.apply(node, printConfig)
                        );
                        out.println(
                                createPrefix(node, printConfig, false) + rows.remove(0)
                        );
                        if (!rows.isEmpty()) {
                            String multiLinePrefix = createPrefix(node, printConfig, true);
                            rows.forEach(
                                    t -> out.println(multiLinePrefix + t)
                            );
                        }
                    }
                }
        );
    }

    public static <T> void printTree(Node<T> tree, PrintConfig config, BiFunction<Node<T>, PrintConfig, String> nodeToStringFunc) {
        printTree(System.out, tree, config, nodeToStringFunc);
    }

    private static List<String> splitContent(String content) {
        int pos = 0;
        List<String> ts = new ArrayList<>();
        int i = 0;
        for (int len = content.length(); i < len; ++i) {
            if (content.charAt(i) == '\n') {
                ts.add(
                        content.substring(pos, i)
                );
                pos = i + 1;
            }
        }
        if (pos < i)
            ts.add(
                    content.substring(pos)
            );
        if (content.endsWith("\n"))
            ts.add("");
        return ts;
    }

    private static <T> String createPrefix(Node<T> node, PrintConfig config, boolean newLine) {
        Node<T> parent = node.getParent();
        if (parent == null)
            return "";
        Node<T> tmpNode = parent;
        StringBuilder sb = new StringBuilder();
        while (tmpNode != null) {
            Node<T> tmpParent = tmpNode.getParent();
            if (tmpParent != null) {
                if (!tmpParent.isRoot() || config.rootVisible) {
                    if (tmpNode.getParent().isLastChild(tmpNode))
                        sb.insert(0, indent);
                    else
                        sb.insert(0, branch2);
                }
            }
            tmpNode = tmpNode.getParent();
        }
        if (!parent.isRoot() || config.rootVisible) {
            if (parent.isLastChild(node)) {
                if (newLine)
                    sb.append(indent);
                else
                    sb.append(end);
            } else {
                if (newLine)
                    sb.append(branch2);
                else
                    sb.append(branch);
            }
        }
        if (newLine) {
            if (node.hasChild())
                sb.append(branch2);
            else
                sb.append(indent);
        }
        return sb.toString();
    }

    public interface NodePrinter<O extends OutputStream, T> {
        void print(O outputStream, Node<T> node, PrintConfig config);
    }

    public static class PrintConfig {
        private final boolean rootVisible;

        public PrintConfig(boolean rootVisible) {
            this.rootVisible = rootVisible;
        }
    }
}
