package agent.server.utils;

import agent.base.utils.IOUtils;
import agent.common.tree.Node;
import agent.common.tree.Tree;
import agent.common.tree.TreeUtils;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ExportUtils {
    private static final ExportUtils instance = new ExportUtils();

    private ExportUtils() {
    }

    public void printStackTrace() {
//        Thread.dumpStack();
        System.out.println("==========================");
        Thread.getAllStackTraces().forEach(
                (thread, stackFrames) -> {
                    System.out.println("---------------------" + thread.getId() + ", " + thread.getName() + "----------------------");
                    Stream.of(stackFrames).forEach(
                            sf -> System.out.println(sf.getClassName() + ": " + sf.getMethodName() + ", " + sf.getLineNumber())
                    );
                }
        );
    }

    private Collection<String> convertToFlameGraphData(Tree<CountItem> tree) {
        Set<String> rs = new TreeSet<>();
        tree.getChildren().forEach(
                subTree -> TreeUtils.traverse(
                        subTree,
                        node -> {
                            if (!node.hasChild())
                                rs.add(
                                        getFullPath(node) + " " + node.getData().count
                                );
                        }
                )
        );
        return rs;
    }

    private String getFullPath(Node<CountItem> node) {
        if (node != null) {
            CountItem data = node.getData();
            if (data != null) {
                String parentPath = getFullPath(
                        node.getParent()
                );
                return parentPath.isEmpty() ?
                        data.name :
                        parentPath + ";" + data.name;
            }
        }
        return "";
    }

    private Tree<CountItem> createTree() {
        Tree<CountItem> tree = new Tree<>();
        Thread.getAllStackTraces().forEach(
                (thread, stackFrames) -> convertStackTraceToTree(
                        tree,
                        thread.getId(),
                        thread.getName(),
                        stackFrames
                )
        );
        return tree;
    }

    private Node<CountItem> getOrCreateNode(Node<CountItem> node, String key, Supplier<String> nameSupplier) {
        Node<CountItem> childNode = node.findFirstChild(
                item -> item.name.equals(key)
        );
        if (childNode == null)
            childNode = node.appendChild(
                    new Node<>(
                            new CountItem(
                                    nameSupplier.get()
                            )
                    )
            );
        else
            childNode.getData().increase();
        return childNode;
    }

    private void convertStackTraceToTree(Tree<CountItem> tree, long threadId, String threadName, StackTraceElement[] sfEls) {
        Node<CountItem> node = getOrCreateNode(
                tree,
                String.valueOf(threadId),
                () -> threadName + "-" + threadId
        );
        for (StackTraceElement el : sfEls) {
            node = getOrCreateNode(
                    node,
                    el.getClassName() + "#" + el.getMethodName(),
                    () -> formatClassName(
                            el.getClassName()
                    ) + ":" + el.getMethodName()
            );
        }
    }

    private String formatClassName(String className) {
        return 'L' + className.replaceAll("\\.", "/");
    }

    private static class CountItem {
        final String name;
        int count;

        private CountItem(String name) {
            this.name = name;
            this.count = 1;
        }

        void increase() {
            count += 1;
        }

        @Override
        public String toString() {
            return name + ": " + count;
        }
    }

    public static void main(String[] args) throws Exception {
//        Map<String, Object> pvs = new HashMap<>();
//        pvs.put("au", instance);
//        ScriptUtils.eval("au.printStackTrace()", pvs);

        Tree<CountItem> tree = instance.createTree();
        TreeUtils.printTree(
                tree,
                new TreeUtils.PrintConfig(false),
                (node, config) -> node.getData().toString()
        );
        System.out.println("==========================");
        Collection<String> data = instance.convertToFlameGraphData(tree);
        IOUtils.write(
                "/home/helowken/cost-time/aaa",
                false,
                writer -> {
                    for (String row : data) {
                        writer.write(row);
                        writer.write('\n');
                    }
                }
        );
    }
}
