package agent.builtin.tools.result;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.builtin.tools.result.parse.CostTimeResultParams;
import agent.base.struct.impl.StructContext;
import agent.common.tree.Node;
import agent.server.transform.impl.DestInvokeIdRegistry;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

abstract class AbstractCostTimeResultHandler<T> extends AbstractResultHandler<T, CostTimeResultParams> {
    private static final Logger logger = Logger.getLogger(AbstractCostTimeResultHandler.class);
    private static final String CACHE_FILE_SUFFIX = "_cache";
    static final StructContext context = new StructContext();

    static {
        context.addPojoInfo(
                CostTimeStatItem.class,
                CostTimeStatItem.POJO_TYPE
        ).setValueSerializeFunc(
                (value, index) -> index == 1 || index == 2 ? value.toString() : value
        ).setValueDeserializeFunc(
                (value, index) -> index == 1 || index == 2 ? new BigDecimal(value.toString()) : value
        );
    }

    private String getCacheFilePath(String inputPath) {
        return inputPath + "." + getCacheType() + CACHE_FILE_SUFFIX;
    }

    @Override
    protected boolean acceptFile(String filePath) {
        return super.acceptFile(filePath) &&
                !filePath.endsWith(CACHE_FILE_SUFFIX);
    }

    @Override
    public void exec(CostTimeResultParams params) {
        logger.debug("Params: {}", params);
        String inputPath = params.getInputPath();
        List<File> dataFiles = findDataFiles(inputPath);
        String cacheFilePath = getCacheFilePath(inputPath);
        File cacheFile = new File(cacheFilePath);
        T result = isCacheAvailable(cacheFile, dataFiles) ?
                readCache(cacheFile) :
                null;
        if (result == null) {
            result = calculateStats(dataFiles, params);
            cacheResult(cacheFilePath, result);
        }
        doPrint(
                readInvokeMetadata(inputPath),
                result,
                params
        );
    }

    private boolean isCacheAvailable(File cacheFile, List<File> dataFiles) {
        if (cacheFile.exists()) {
            long dataFileMaxLastModified = -1;
            for (File dataFile : dataFiles) {
                dataFileMaxLastModified = Math.max(dataFile.lastModified(), dataFileMaxLastModified);
            }
            return cacheFile.lastModified() >= dataFileMaxLastModified;
        }
        return false;
    }

    private T readCache(File cacheFile) {
        String cacheFilePath = cacheFile.getAbsolutePath();
        if (cacheFile.exists()) {
            try {
                System.out.println("Read data from cache file: " + cacheFilePath);
                return deserializeResult(
                        IOUtils.readBytes(cacheFilePath)
                );
            } catch (Exception e) {
                logger.error("Print with cache failed: {}", e, cacheFilePath);
            }
        }
        return null;
    }

    private void cacheResult(String cacheFilePath, T result) {
        try {
            IOUtils.writeBytes(
                    cacheFilePath,
                    serializeResult(result),
                    false
            );
        } catch (Throwable t) {
            t.printStackTrace();
            logger.error("Save result to file failed: {}", t, cacheFilePath);
        }
    }

    void doCalculateFile(File dataFile, CostTimeCalculateFunc calculateFunc) {
        calculateBinaryFile(
                dataFile,
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

    Node<String> newInvokeNode(String invoke, CostTimeStatItem item, CostTimeResultParams params) {
//        Set<Float> range = params.getRange();
        return new Node<>(
                "[" + item.getAvgTimeString() + ", " + item.getCountString() + "] " +
//                        (range != null ? " " + item.getTimeDistributionString(range) + " " : "") +
                        invoke
        );
    }

    abstract String getCacheType();

    abstract byte[] serializeResult(T result);

    abstract T deserializeResult(byte[] content);

    abstract void doPrint(Map<Integer, DestInvokeIdRegistry.InvokeMetadata> idToClassInvoke, T result, CostTimeResultParams params);

    interface CostTimeCalculateFunc {
        void exec(int id, int parentId, int invokeId, int costTime, boolean error);
    }

}

