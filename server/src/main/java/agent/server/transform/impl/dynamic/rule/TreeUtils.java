package agent.server.transform.impl.dynamic.rule;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

public class TreeUtils {
    public static <T> void traverseTree(RuleNode<T> tree, NodeAccessor<T> nodeAccessor) {
        LinkedList<RuleNode<T>> leftNodes = new LinkedList<>();
        leftNodes.add(tree);
        while (!leftNodes.isEmpty()) {
            RuleNode<T> node = leftNodes.pop();
            nodeAccessor.access(node);
            leftNodes.addAll(0, node.getChildren());
        }
    }

    public static <T> void printTree(RuleNode<T> tree, DataPrinter<PrintStream, T> dataPrinter) {
        printTree(System.out, tree, dataPrinter);
    }

    public static <O extends OutputStream, T> void printTree(O outputStream, RuleNode<T> tree, DataPrinter<O, T> dataPrinter) {
        traverseTree(tree, node -> dataPrinter.print(outputStream, node));
    }

    public interface DataPrinter<O extends OutputStream, T> {
        void print(O outputStream, RuleNode<T> node);
    }

    public interface NodeAccessor<T> {
        void access(RuleNode<T> node);
    }
}
