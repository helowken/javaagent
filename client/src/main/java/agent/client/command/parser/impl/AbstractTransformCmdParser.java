package agent.client.command.parser.impl;

import agent.base.utils.Utils;
import agent.cmdline.args.parse.CmdParamParser;
import agent.cmdline.args.parse.CmdParams;
import agent.cmdline.args.parse.DefaultParamParser;
import agent.cmdline.command.Command;
import agent.cmdline.exception.ArgMissingException;
import agent.common.config.ModuleConfig;
import agent.common.config.TransformerConfig;
import agent.common.message.command.DefaultCommand;

import java.util.Collections;

import static agent.common.args.parse.FilterOptUtils.getFilterOptParsers;
import static agent.common.message.MessageType.CMD_TRANSFORM;

abstract class AbstractTransformCmdParser extends AbstractModuleCmdParser {
    abstract String getTransformerKey();

    @Override
    protected CmdParamParser<CmdParams> createParamParser() {
        return new DefaultParamParser(
                getFilterOptParsers()
        );
    }

    @Override
    protected void checkParams(CmdParams params) throws Exception {
        super.checkParams(params);
        if (Utils.isBlank(getTransformerKey()))
            throw new ArgMissingException("Transformer key");
    }

    @Override
    Command newCommand(Object data) {
        return new DefaultCommand(CMD_TRANSFORM, data);
    }

    @Override
    ModuleConfig createModuleConfig(CmdParams params) {
        ModuleConfig moduleConfig = super.createModuleConfig(params);
        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setId(
                params.getArgs()[0]
        );
        transformerConfig.setRef(
                getTransformerKey()
        );
        setConfig(transformerConfig, params);
        moduleConfig.setTransformers(
                Collections.singletonList(transformerConfig)
        );
        return moduleConfig;
    }

    void setConfig(TransformerConfig transformerConfig, CmdParams params) {
    }
}
