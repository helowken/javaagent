package agent.client.command.parser.impl;

import agent.base.utils.Utils;
import agent.client.args.parse.TransformParamParser;
import agent.client.args.parse.TransformParams;
import agent.common.config.ModuleConfig;
import agent.common.config.TransformerConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.TransformCommand;

import java.util.Collections;
import java.util.Map;

abstract class AbstractTransformCmdParser extends AbstractModuleCmdParser<TransformParams> {
    private static final String REF_SEP = ":";
    private static final TransformParamParser parser = new TransformParamParser();

    abstract String getTransformerKey();

    @Override
    TransformParams doParse(String[] args) {
        return parser.parse(args);
    }

    @Override
    protected void checkParams(TransformParams params) {
        super.checkParams(params);
        if (Utils.isBlank(getTransformerKey()))
            throw new RuntimeException("No transformer key found.");
        params.getOutputPath();
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
                                params.getTransformerId(),
                                params.getOutputPath()
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
