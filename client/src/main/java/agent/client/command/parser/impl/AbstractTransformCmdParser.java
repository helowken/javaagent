package agent.client.command.parser.impl;

import agent.base.utils.FileUtils;
import agent.base.utils.Utils;
import agent.common.config.ModuleConfig;
import agent.common.config.TransformerConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformCommand;
import agent.common.parser.BasicParams;
import agent.common.parser.ChainOptions;

import java.util.Collections;
import java.util.Map;

abstract class AbstractTransformCmdParser extends AbstractFilterCmdParser<TransformFilterOptions, TransformParams> {
    private static final String REF_SEP = ":";
    private static final String OUTPUT_PATH = "outputPath";
    private static final String OPT_TRANSFORMER_ID = "-t";
    private static final String PARAM_CMD = "cmd";

    abstract String getTransformerKey();

    @Override
    protected TransformFilterOptions createFilterOptions() {
        return new TransformFilterOptions();
    }

    @Override
    protected TransformParams createParams() {
        return new TransformParams();
    }

    @Override
    protected String getMsgFile() {
        return "transform.txt";
    }

    @Override
    protected String getUsageParamValue(String param) {
        return PARAM_CMD.equals(param) ?
                getCmdName() :
                super.getUsageParamValue(param);
    }

    @Override
    protected int parseOption(TransformFilterOptions opts, String[] args, int currIdx) {
        int i = currIdx;
        switch (args[i]) {
            case OPT_TRANSFORMER_ID:
                opts.transformerId = getArg(args, ++i, "transformerId");
                break;
            default:
                return super.parseOption(opts, args, currIdx);
        }
        return i;
    }

    @Override
    protected void parseAfterOptions(TransformParams params, String[] args, int startIdx) {
        int i = startIdx;
        params.outputPath = FileUtils.getAbsolutePath(
                getArg(args, i, OUTPUT_PATH)
        );
    }

    @Override
    protected void checkParams(TransformParams params) {
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
                                params.opts.transformerId,
                                params.outputPath
                        )
                )
        );
        return moduleConfig;
    }

    private String newRef(String type, String transformerId) {
        String ref = type;
        if (Utils.isNotBlank(transformerId))
            ref += REF_SEP + transformerId;
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

class TransformParams extends BasicParams<TransformFilterOptions> {
    String outputPath;
}

class TransformFilterOptions extends ChainOptions {
    String transformerId;
}
