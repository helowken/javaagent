package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.args.parse.KeyValueOptParser;
import agent.base.exception.ArgMissingException;
import agent.base.help.HelpArg;
import agent.base.utils.FileUtils;
import agent.base.utils.Utils;
import agent.client.args.parse.DefaultParamParser;
import agent.client.args.parse.TransformOptConfigs;
import agent.common.config.ModuleConfig;
import agent.common.config.TransformerConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.MapCommand;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static agent.common.args.parse.FilterOptUtils.getFilterAndChainOptParsers;
import static agent.common.args.parse.FilterOptUtils.merge;
import static agent.common.message.MessageType.CMD_TRANSFORM;

abstract class AbstractTransformCmdParser extends AbstractModuleCmdParser {
    private static final String REF_SEP = ":";

    abstract String getTransformerKey();

    @Override
    List<HelpArg> createHelpArgList() {
        return Collections.singletonList(
                new HelpArg(
                        "OUTPUT_PATH",
                        "File path used to store data."
                )
        );
    }

    @Override
    CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                merge(
                        getFilterAndChainOptParsers(),
                        new KeyValueOptParser(
                                TransformOptConfigs.getSuite()
                        )
                )
        );
    }

    @Override
    protected void checkParams(CmdParams params) {
        super.checkParams(params);
        if (Utils.isBlank(getTransformerKey()))
            throw new ArgMissingException("Transformer key");
        params.getArgsOpts().getArg(0, "OUTPUT_PATH");
    }

    @Override
    Command newCommand(Map<String, Object> data) {
        return new MapCommand(CMD_TRANSFORM, data);
    }

    @Override
    ModuleConfig createModuleConfig(CmdParams params) {
        ModuleConfig moduleConfig = super.createModuleConfig(params);
        moduleConfig.setTransformers(
                Collections.singletonList(
                        createTransformerConfig(
                                getTransformerKey(),
                                TransformOptConfigs.getTransformId(
                                        params.getOpts()
                                ),
                                FileUtils.getAbsolutePath(
                                        params.getArgs()[0]
                                )
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
