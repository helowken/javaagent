package agent.server.tree;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.function.BiFunction;

public class TreeUtils {
    private static final String branch = "├─";
    private static final String branch2 = "│   ";
    private static final String end = "└─";
    private static final String indent = "    ";
    private static final PrintConfig defaultConfig = new PrintConfig(true);

    public static <T> void traverseTree(Node<T> tree, NodeAccessor<T> nodeAccessor) {
        LinkedList<Node<T>> leftNodes = new LinkedList<>();
        leftNodes.add(tree);
        while (!leftNodes.isEmpty()) {
            Node<T> node = leftNodes.pop();
            nodeAccessor.access(node);
            leftNodes.addAll(0, node.getChildren());
        }
    }

    public static <O extends OutputStream, T> void printTree(O outputStream, Node<T> tree, PrintConfig config, NodePrinter<O, T> nodePrinter) {
        traverseTree(
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
                    if (!node.isRoot() || printConfig.rootVisible)
                        out.println(
                                createPrefix(node, printConfig) +
                                        nodeToStringFunc.apply(node, printConfig)
                        );
                }
        );
    }

    public static <T> void printTree(Node<T> tree, PrintConfig config, BiFunction<Node<T>, PrintConfig, String> nodeToStringFunc) {
        printTree(System.out, tree, config, nodeToStringFunc);
    }

    private static <T> String createPrefix(Node<T> node, PrintConfig config) {
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
            if (parent.isLastChild(node))
                sb.append(end);
            else
                sb.append(branch);
        }
        return sb.toString();
    }

    public interface NodePrinter<O extends OutputStream, T> {
        void print(O outputStream, Node<T> node, PrintConfig config);
    }

    public interface NodeAccessor<T> {
        void access(Node<T> node);
    }

    public static class PrintConfig {
        private final boolean rootVisible;

        public PrintConfig(boolean rootVisible) {
            this.rootVisible = rootVisible;
        }
    }
}
