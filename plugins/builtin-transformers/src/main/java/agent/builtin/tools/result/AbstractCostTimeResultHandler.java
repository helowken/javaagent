package agent.builtin.tools.result;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.common.tree.Node;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractCostTimeResultHandler<T>
        extends AbstractResultHandler<T, CostTimeStatItem, CostTimeResultFilter, CostTimeResultOptions, CostTimeResultParams> {
    private static final Logger logger = Logger.getLogger(AbstractCostTimeResultHandler.class);
    private static final String CACHE_FILE_SUFFIX = "_cache";

    private String getCacheFilePath(String inputPath) {
        return inputPath + "." + getCacheType() + CACHE_FILE_SUFFIX;
    }

    @Override
    protected boolean acceptFile(String filePath) {
        return super.acceptFile(filePath) &&
                !filePath.endsWith(CACHE_FILE_SUFFIX);
    }

    @Override
    CostTimeResultFilter createFilter() {
        return new CostTimeResultFilter();
    }

    @Override
    public void exec(CostTimeResultParams params) throws Exception {
        String inputPath = params.inputPath;
        String cacheFilePath = getCacheFilePath(inputPath);
        T result = readCache(cacheFilePath);
        if (result == null) {
            result = calculateStats(inputPath, params);
            cacheResult(cacheFilePath, result);
        }
        doPrint(
                readMetadata(inputPath),
                result,
                params
        );
    }

    private T readCache(String cacheFilePath) {
        File cacheFile = new File(cacheFilePath);
        if (cacheFile.exists()) {
            try {
                System.out.println("Read data from cache file: " + cacheFilePath);
                return deserializeResult(
                        IOUtils.readToString(cacheFilePath)
                );
            } catch (Exception e) {
                logger.error("Print with cache failed: {}", e, cacheFilePath);
            }
        }
        return null;
    }

    private void cacheResult(String cacheFilePath, T result) {
        try {
            IOUtils.writeString(
                    cacheFilePath,
                    serializeResult(result),
                    false
            );
        } catch (Throwable t) {
            logger.error("Save result to file failed: {}", t, cacheFilePath);
        }
    }

    void doCalculateFile(String dataFilePath, CostTimeCalculateFunc calculateFunc) {
        calculateBytesFile(
                dataFilePath,
                in -> {
                    int totalSize = 0;
                    int count = in.readInt();
                    totalSize += Integer.BYTES;
                    for (int i = 0; i < count; ++i) {
                        int id = in.readInt();
                        int parentId = in.readInt();
                        int invokeId = in.readInt();
                        int costTime = in.readInt();
                        boolean error = in.readByte() == 1;
                        calculateFunc.exec(id, parentId, invokeId, costTime, error);
                        totalSize += Integer.BYTES * 4 + Byte.BYTES;
                    }
                    return totalSize;
                }
        );
    }

    Node<String> newInvokeNode(String invoke, CostTimeStatItem item, Set<Float> rates) {
        return new Node<>(
//                invoke + "\n" +
//                        item.getAvgTimeString() + "\n" +
//                        item.getMaxTimeString() + "\n" +
//                        item.getCountString() + "\n" +
//                        item.getTimeDistributionString(rates) + "\n\n"
                "[" + item.getAvgTimeString() + ", " + item.getCountString() + "] " + invoke
        );
    }

    abstract String getCacheType();

    abstract String serializeResult(T result);

    abstract T deserializeResult(String content);

    abstract void doPrint(List<Map<String, Map<String, Integer>>> classToInvokeToId, T result, CostTimeResultParams params);

    interface CostTimeCalculateFunc {
        void exec(int id, int parentId, int invokeId, int costTime, boolean error);
    }

}


class CostTimeResultFilter extends ResultFilter<CostTimeStatItem> {
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_MAX_TIME = "maxTime";
    private static final String PARAM_AVG_TIME = "avgTime";

    @Override
    Map<String, Object> convertTo(CostTimeStatItem item) {
        Map<String, Object> pvs = new HashMap<>();
        pvs.put(PARAM_COUNT, item.getCount());
        pvs.put(PARAM_AVG_TIME, item.getAvgTime());
        pvs.put(PARAM_MAX_TIME, item.getMaxTime());
        return pvs;
    }
}
