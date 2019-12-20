package agent.builtin.transformer;

import agent.base.utils.MethodDescriptorUtils;
import agent.base.utils.Utils;
import agent.builtin.transformer.utils.CostTimeMethodRegistry;
import agent.server.transform.impl.ProxyAnnotationConfig;
import agent.server.transform.impl.ProxyAnnotationConfigTransformer;
import agent.server.utils.log.LogConfig;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.binary.BinaryLogItem;
import agent.server.utils.log.binary.BinaryLogItemPool;

import java.lang.reflect.Method;
import java.util.*;

import static agent.server.transform.impl.ProxyAnnotationConfig.*;

@SuppressWarnings("unchecked")
public class CostTimeStatisticsTransformer extends ProxyAnnotationConfigTransformer {
    public static final String REG_KEY = "sys_costTimeStatistics";
    private static final CostTimeStatisticsConfig statisticsConfig = new CostTimeStatisticsConfig();

    private Map<Method, Integer> methodToId = new HashMap<>();
    private String logKey;

    @Override
    protected void doSetConfig(Map<String, Object> config) {
        logKey = LogMgr.regBinary(config, Collections.EMPTY_MAP);
        LogConfig logConfig = LogMgr.getBinaryLogConfig(logKey);
        CostTimeMethodRegistry.getInstance().regOutputPath(
                getContext(),
                logConfig.getOutputPath()
        );
    }

    @Override
    protected void preTransformMethod(Method method) {
        methodToId.computeIfAbsent(
                method,
                key -> CostTimeMethodRegistry.getInstance().reg(
                        getContext(),
                        method.getDeclaringClass().getName(),
                        MethodDescriptorUtils.getFullDescriptor(method)
                )
        );
    }

    @Override
    protected Object getInstanceForMethod(Method method) {
        return statisticsConfig;
    }

    @Override
    protected Set<Class<?>> getAnnotationClasses() {
        return Collections.singleton(
                CostTimeStatisticsConfig.class
        );
    }

    private int getMethodId(Method method) {
        return Optional.ofNullable(
                methodToId.get(method)
        ).orElseThrow(
                () -> new RuntimeException("No id found for method: " + method)
        );
    }

    @Override
    protected Object[] newOtherArgs(Method srcMethod, Method anntMethod, int argsHint) {
        switch (argsHint) {
            case ARGS_ON_BEFORE:
                return new Object[]{
                        getMethodId(srcMethod),
                        logKey
                };
            case ARGS_ON_RETURNING:
                return new Object[]{
                        getMethodId(srcMethod)
                };
            case ARGS_ON_AFTER:
                return new Object[]{
                        logKey
                };
        }
        return null;
    }

    @Override
    public String getRegKey() {
        return REG_KEY;
    }


    static class CostTimeStatisticsConfig extends ProxyAnnotationConfig<CostTimeItem> {
        @Override
        protected CostTimeItem newData(Node<CostTimeItem> preNode, Object[] args, Class<?>[] argTypes, Method method, Object[] otherArgs) {
            CostTimeItem item;
            int parentMethodId;
            if (preNode == null) {
                item = new CostTimeItem();
                parentMethodId = -1;
            } else {
                item = preNode.getData();
                parentMethodId = item.peek().methodId;
            }
            int methodId = Utils.getArgValue(otherArgs, 0);
            item.add(
                    new MethodItem(
                            parentMethodId,
                            methodId,
                            System.currentTimeMillis()
                    )
            );
            return item;
        }

        @Override
        protected void processOnReturning(Node<CostTimeItem> currNode, Object returnValue, Class<?> returnType, Method method, Object[] otherArgs) {
            currNode.getData().finish(
                    System.currentTimeMillis(),
                    false
            );
        }

        @Override
        protected void processOnThrowing(Node<CostTimeItem> currNode, Throwable error, Method method, Object[] otherArgs) {
            currNode.getData().finish(
                    System.currentTimeMillis(),
                    true
            );
        }

        @Override
        protected void processOnAfter(Node<CostTimeItem> currNode, Method method, Object[] otherArgs) {
            if (currNode.isRoot()) {
                String logKey = Utils.getArgValue(otherArgs, 0);
                BinaryLogItem logItem = BinaryLogItemPool.get(logKey);
                currNode.getData().writeTo(logItem);
                LogMgr.logBinary(logKey, logItem);
            }
        }
    }

    private static class MethodItem {
        final int parentMethodId;
        final int methodId;
        final long startTime;
        long endTime;
        boolean error;

        private MethodItem(int parentMethodId, int methodId, long startTime) {
            this.parentMethodId = parentMethodId;
            this.methodId = methodId;
            this.startTime = startTime;
        }
    }

    private static class CostTimeItem {
        private Stack<MethodItem> undergoing = new Stack<>();
        private List<MethodItem> completed = new ArrayList<>(100);

        void add(MethodItem methodTime) {
            undergoing.push(methodTime);
        }

        MethodItem peek() {
            if (undergoing.isEmpty())
                throw new RuntimeException("Undergoing is empty.");
            return undergoing.peek();
        }

        void finish(long et, boolean error) {
            if (undergoing.empty())
                throw new RuntimeException("Undergoing is empty.");
            MethodItem item = undergoing.pop();
            item.endTime = et;
            item.error = error;
            completed.add(item);
        }

        void writeTo(BinaryLogItem logItem) {
            if (!undergoing.isEmpty())
                throw new RuntimeException("Undergoing is not empty.");
            logItem.putInt(
                    completed.size()
            );
            completed.forEach(
                    item -> {
                        logItem.putInt(item.parentMethodId);
                        logItem.putInt(item.methodId);
                        logItem.putInt((int) (item.endTime - item.startTime));
                        logItem.put((byte) (item.error ? 1 : 0));
                    }
            );
        }
    }
}
