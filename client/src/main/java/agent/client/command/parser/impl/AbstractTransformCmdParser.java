package agent.client.command.parser.impl;

import agent.common.config.ModuleConfig;
import agent.common.config.TransformerConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformCommand;

import java.util.Collections;
import java.util.Map;

abstract class AbstractTransformCmdParser extends AbstractFilterCmdParser<TransformParams> {
    private static final String OUTPUT_PATH = "outputPath";

    abstract String getTransformerKey();

    @Override
    String getUsageMsg() {
        return super.getUsageMsg() + " " + OUTPUT_PATH;
    }

    @Override
    TransformParams createParams(String[] args) {
        TransformParams params = new TransformParams();
        int i = 0;
        params.contextPath = getContext(args, i++);
        params.filterOptions = parseOptions(args, i, args.length - 1);
        params.outputPath = getArg(args, params.filterOptions.nextIdx, OUTPUT_PATH);
        checkNotBlank(
                getTransformerKey(),
                params.contextPath,
                params.filterOptions.classStr,
                params.outputPath
        );
        return params;
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
                                params.outputPath
                        )
                )
        );
        return moduleConfig;
    }

    private TransformerConfig createTransformerConfig(String type, String outputPath) {
        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setRef(type);
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

class TransformParams extends FilterParams {
    String outputPath;
}
