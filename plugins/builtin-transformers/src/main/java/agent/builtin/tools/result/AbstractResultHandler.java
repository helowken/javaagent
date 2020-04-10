package agent.builtin.tools.result;

import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.InvokeDescriptorUtils;
import agent.base.utils.InvokeDescriptorUtils.TextConfig;
import agent.base.utils.Utils;
import agent.common.config.ClassFilterConfig;
import agent.common.config.ConstructorFilterConfig;
import agent.common.config.MethodFilterConfig;
import agent.common.config.TargetConfig;
import agent.common.utils.JSONUtils;
import agent.server.transform.impl.DestInvokeIdRegistry;
import agent.server.transform.search.filter.FilterUtils;
import agent.server.transform.search.filter.ScriptFilter;

import java.io.*;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static agent.common.parser.FilterOptionUtils.createTargetConfig;

abstract class AbstractResultHandler<T, D, F extends ResultFilter<D>> {
    abstract T calculate(Collection<String> dataFiles);

    abstract F createFilter();

    private List<String> findDataFiles(String dataFilePath) throws FileNotFoundException {
        File dir = FileUtils.getValidFile(dataFilePath).getParentFile();
        List<String> dataFiles = null;
        if (dir != null) {
            File[] files = dir.listFiles();
            if (files != null) {
                dataFiles = Stream.of(files)
                        .map(File::getAbsolutePath)
                        .filter(
                                filePath -> {
                                    if (filePath.equals(dataFilePath))
                                        return true;
                                    int pos = filePath.lastIndexOf(".");
                                    String tmpPath = filePath;
                                    if (pos > -1)
                                        tmpPath = filePath.substring(0, pos);
                                    return tmpPath.equals(dataFilePath) && acceptFile(filePath);
                                }
                        )
                        .collect(Collectors.toList());
            }
        }
        if (dataFiles == null || dataFiles.isEmpty())
            throw new FileNotFoundException("No data files found in dir of file: " + dataFilePath);
        return dataFiles;
    }

    protected boolean acceptFile(String filePath) {
        return !DestInvokeIdRegistry.isMetadataFile(filePath);
    }

    T calculateStats(String inputPath) throws Exception {
        List<String> dataFilePaths = findDataFiles(inputPath);
        long st = System.currentTimeMillis();
        ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() - 1);
        try {
            return pool.submit(
                    () -> calculate(dataFilePaths)
            ).get();
        } finally {
            long et = System.currentTimeMillis();
            System.out.println("Result calculation used time: " + (et - st) + "ms");
            pool.shutdown();
        }
    }

    List<Map<String, Map<String, Integer>>> readMetadata(String inputPath) throws IOException {
        String[] metadataPaths = DestInvokeIdRegistry.getMetadataFiles(inputPath);
        List<Map<String, Map<String, Integer>>> metadataList = new ArrayList<>();
        for (String path : metadataPaths) {
            metadataList.add(
                    JSONUtils.read(
                            IOUtils.readToString(
                                    FileUtils.getValidFile(path).getAbsolutePath()
                            )
                    )
            );
        }
        return metadataList;
    }

    Map<Integer, InvokeMetadata> convertMetadata(List<Map<String, Map<String, Integer>>> classToInvokeToIdList) {
        Map<Integer, InvokeMetadata> rsMap = new HashMap<>();
        classToInvokeToIdList.forEach(
                classToInvokeToId -> classToInvokeToId.forEach(
                        (clazz, invokeToId) -> invokeToId.forEach(
                                (invoke, id) -> rsMap.put(
                                        id,
                                        new InvokeMetadata(clazz, invoke)
                                )
                        )
                )
        );
        return rsMap;
    }

    String formatInvoke(String method) {
        TextConfig config = new TextConfig();
        config.withReturnType = false;
        config.withPkg = false;
        return InvokeDescriptorUtils.descToText(method, config);
    }

    String formatClassName(String className) {
        return InvokeDescriptorUtils.getSimpleName(className);
    }

    void calculateBytesFile(String dataFilePath, CalculateBytesFunc calculateFunc) {
        calculateFile(
                dataFilePath,
                inputFile -> {
                    long length = inputFile.length();
                    try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)))) {
                        while (length > 0) {
                            length -= calculateFunc.exec(in);
                            if (length < 0)
                                throw new RuntimeException("Invalid calculation.");
                        }
                    }
                }
        );
    }

    void calculateTextFile(String dataFilePath, CalculateTextFunc calculateTextFunc) {
        calculateFile(
                dataFilePath,
                inputFile -> {
                    try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                        calculateTextFunc.exec(reader);
                    }
                }
        );
    }

    InvokeMetadata getMetadata(Map<Integer, InvokeMetadata> idToInvoke, Integer invokeId) {
        return Optional.ofNullable(
                idToInvoke.get(invokeId)
        ).orElseThrow(
                () -> new RuntimeException("No metadata found for invoke id: " + invokeId)
        );
    }

    String convertInvoke(Integer parentInvokeId, Map<Integer, InvokeMetadata> idToInvoke, InvokeMetadata metadata) {
        String invoke = formatInvoke(metadata.invoke);
        if (parentInvokeId == null)
            invoke = formatClassName(metadata.clazz) + " # " + invoke;
        else {
            InvokeMetadata parentMetadata = getMetadata(idToInvoke, parentInvokeId);
            if (!parentMetadata.clazz.equals(metadata.clazz))
                invoke = formatClassName(metadata.clazz) + " # " + invoke;
        }
        return invoke;
    }

    private void calculateFile(String dataFilePath, ProcessFileFunc processFileFunc) {
        try {
            long st = System.currentTimeMillis();
            processFileFunc.process(
                    new File(dataFilePath)
            );
            long et = System.currentTimeMillis();
            System.out.println("Calculate " + dataFilePath + " used time: " + (et - st) + "ms");
        } catch (Exception e) {
            System.err.println("Read data file failed: " + dataFilePath + "\n" + Utils.getErrorStackStrace(e));
        }
    }

    F newFilter(ResultOptions opts) {
        TargetConfig targetConfig = createTargetConfig(opts);
        ClassFilterConfig classFilterConfig = targetConfig.getClassFilter();
        MethodFilterConfig methodFilterConfig = targetConfig.getMethodFilter();
        ConstructorFilterConfig constructorFilterConfig = targetConfig.getConstructorFilter();
        F filter = createFilter();
        if (classFilterConfig != null)
            filter.setClassFilter(
                    FilterUtils.newClassStringFilter(
                            classFilterConfig.getIncludes(),
                            classFilterConfig.getExcludes()
                    )
            );
        if (methodFilterConfig != null)
            filter.setMethodFilter(
                    FilterUtils.newInvokeStringFilter(
                            methodFilterConfig.getIncludes(),
                            methodFilterConfig.getExcludes()
                    )
            );
        if (constructorFilterConfig != null)
            filter.setConstructorFilter(
                    FilterUtils.newInvokeStringFilter(
                            constructorFilterConfig.getIncludes(),
                            constructorFilterConfig.getExcludes()
                    )
            );
        if (opts.filterExpr != null)
            filter.setScriptFilter(
                    new ScriptFilter(opts.filterExpr)
            );
        return filter;
    }

    private interface ProcessFileFunc {
        void process(File dataFilePath) throws Exception;
    }

    interface CalculateBytesFunc {
        int exec(DataInputStream in) throws Exception;
    }

    interface CalculateTextFunc {
        void exec(BufferedReader reader) throws Exception;
    }

}

class InvokeMetadata {
    final String clazz;
    final String invoke;

    InvokeMetadata(String clazz, String invoke) {
        this.clazz = clazz;
        this.invoke = invoke;
    }
}

