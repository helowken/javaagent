package agent.client.command.parser.impl;

import agent.base.args.parse.CmdParamParser;
import agent.base.args.parse.CmdParams;
import agent.base.args.parse.KeyValueOptParser;
import agent.base.exception.ArgMissingException;
import agent.base.utils.Utils;
import agent.client.args.parse.DefaultParamParser;
import agent.client.args.parse.TransformOptConfigs;
import agent.common.config.ModuleConfig;
import agent.common.config.TransformerConfig;
import agent.common.message.command.Command;
import agent.common.message.command.impl.PojoCommand;

import java.util.Collections;

import static agent.common.args.parse.FilterOptUtils.getFilterAndChainOptParsers;
import static agent.common.args.parse.FilterOptUtils.merge;
import static agent.common.message.MessageType.CMD_TRANSFORM;

abstract class AbstractTransformCmdParser extends AbstractModuleCmdParser {
    private static final String REF_SEP = ":";

    abstract String getTransformerKey();

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
    }

    @Override
    Command newCommand(Object data) {
        return new PojoCommand(CMD_TRANSFORM, data);
    }

    @Override
    ModuleConfig createModuleConfig(CmdParams params) {
        ModuleConfig moduleConfig = super.createModuleConfig(params);
        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setRef(
                newRef(
                        getTransformerKey(),
                        TransformOptConfigs.getTransformId(
                                params.getOpts()
                        )
                )
        );
        setConfig(transformerConfig, params);
        moduleConfig.setTransformers(
                Collections.singletonList(transformerConfig)
        );
        return moduleConfig;
    }

    private String newRef(String type, String transformerId) {
        String ref = type;
        if (Utils.isNotBlank(transformerId))
            ref += REF_SEP + transformerId;
        return ref;
    }

    void setConfig(TransformerConfig transformerConfig, CmdParams params) {
    }
}
