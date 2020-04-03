package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.base.utils.Utils;
import agent.common.config.ModuleConfig;
import agent.common.config.TransformerConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformCommand;

import java.util.Collections;
import java.util.Map;

abstract class AbstractTransformCmdParser extends AbstractFilterCmdParser<TransformFilterOptions, TransformParams> {
    private static final String SEP = ":";
    private static final String OUTPUT_PATH = "outputPath";
    private static final String OPT_TRANSFORMER_ID = "-t";

    abstract String getTransformerKey();

    TransformFilterOptions createFilterOptions() {
        return new TransformFilterOptions();
    }

    @Override
    String getMsgFile() {
        return "transform.txt";
    }

    @Override
    String getUsageMsg() {
        return super.getUsageMsg() + " " + OUTPUT_PATH;
    }

    @Override
    boolean parseOtherOptions(TransformFilterOptions opts, String[] args, int currIdx, int endIdx) {
        int i = currIdx;
        if (args[i].equals(OPT_TRANSFORMER_ID)) {
            opts.transformerId = getArg(args, ++i, "transformerId");
            opts.nextIdx = i;
            return true;
        }
        return false;
    }

    @Override
    TransformParams createParams(String[] args) {
        TransformParams params = new TransformParams();
        params.filterOptions = parseOptions(args, args.length - 2);
        int i = params.filterOptions.nextIdx;
        params.contextPath = getContext(args, i++);
        params.outputPath = FileUtils.getAbsolutePath(
                getArg(args, i, OUTPUT_PATH)
        );
        return params;
    }

    @Override
    void checkParams(TransformParams params) {
        super.checkParams(params);
        checkNotBlank(
                getTransformerKey(),
                "transformerKey"
        );
        checkNotBlank(
                params.outputPath,
                OUTPUT_PATH
        );
    }

    @Override
    Command createCommand(Map<String, Object> data) {
        return new TransformCommand(data);
    }

    @Override
    ModuleConfig createModuleConfig(TransformParams params) {
        ModuleConfig moduleConfig = super.createModuleConfig(params);
        moduleConfig.setTransformers(
                Collections.singletonList(
                        createTransformerConfig(
                                getTransformerKey(),
                                params.filterOptions.transformerId,
                                params.outputPath
                        )
                )
        );
        return moduleConfig;
    }

    private String newRef(String type, String transformerId) {
        String ref = type;
        if (Utils.isNotBlank(transformerId))
            ref += SEP + transformerId;
        return ref;
    }

    private TransformerConfig createTransformerConfig(String type, String transformerId, String outputPath) {
        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setRef(
                newRef(type, transformerId)
        );
        if (outputPath != null)
            transformerConfig.setConfig(
                    newConfigOfTransformer(outputPath)
            );
        return transformerConfig;
    }

    private Map<String, Object> newConfigOfTransformer(String outputPath) {
        return Collections.singletonMap(
                "log",
                Collections.singletonMap(
                        "outputPath",
                        outputPath
                )
        );
    }
}

class TransformParams extends FilterParams<TransformFilterOptions> {
    String outputPath;
}

class TransformFilterOptions extends FilterOptions {
    String transformerId;
}
