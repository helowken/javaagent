package agent.builtin.tools.execute.handler;

import agent.base.struct.impl.StructContext;
import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.builtin.tools.config.CostTimeResultConfig;
import agent.builtin.tools.execute.ResultExecUtils;
import agent.builtin.tools.result.data.CostTimeStatItem;
import agent.common.tree.Node;
import agent.common.utils.MetadataUtils;
import agent.server.transform.impl.DestInvokeIdRegistry;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

abstract class AbstractCostTimeResultHandler<T> {
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

    abstract String getCacheType();

    abstract byte[] serializeResult(T result);

    abstract T deserializeResult(byte[] content);

    abstract T calculate(List<File> dataFiles, CostTimeResultConfig config);

    abstract void doPrint(Map<Integer, DestInvokeIdRegistry.InvokeMetadata> idToClassInvoke, T result, CostTimeResultConfig config);

    private String getCacheFilePath(String inputPath) {
        return inputPath + "." + getCacheType() + CACHE_FILE_SUFFIX;
    }

    public void process(CostTimeResultConfig config) {
        String inputPath = config.getInputPath();
        List<File> dataFiles = ResultExecUtils.findDataFiles(
                inputPath,
                filePath -> !MetadataUtils.isMetadataFile(filePath) &&
                        !filePath.endsWith(CACHE_FILE_SUFFIX)
        );
        String cacheFilePath = getCacheFilePath(inputPath);
        File cacheFile = new File(cacheFilePath);
        T result = isCacheAvailable(cacheFile, dataFiles) ?
                readCache(cacheFile) :
                null;
        if (result == null) {
            result = ResultExecUtils.calculateAll(
                    () -> calculate(dataFiles, config)
            );
            cacheResult(cacheFilePath, result);
        }
        doPrint(
                ResultExecUtils.readInvokeMetadata(inputPath),
                result,
                config
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
        ResultExecUtils.calculateBinaryFile(
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

    static Node<String> newInvokeNode(String invoke, CostTimeStatItem item, CostTimeResultConfig config) {
//        Set<Float> range = config.getRange();
        return new Node<>(
                "[" + item.getAvgTimeString() + ", " + item.getCountString() + "] " +
//                        (range != null ? " " + item.getTimeDistributionString(range) + " " : "") +
                        invoke
        );
    }

    interface CostTimeCalculateFunc {
        void exec(int id, int parentId, int invokeId, int costTime, boolean error);
    }

}

