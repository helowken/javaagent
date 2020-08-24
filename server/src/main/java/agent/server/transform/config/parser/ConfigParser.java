package agent.server.transform.config.parser;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.base.utils.TypeObject;
import agent.common.config.ModuleConfig;
import agent.common.utils.JsonUtils;
import agent.server.transform.config.parser.exception.ConfigParseException;

import java.io.File;
import java.util.Map;

public class ConfigParser {
    private static final Logger logger = Logger.getLogger(ConfigParser.class);

    public static ModuleConfig parse(Object source) throws ConfigParseException {
        try {
            Object data = source;
            if (data instanceof File)
                data = IOUtils.readBytes(((File) data).getAbsolutePath());
            if (data instanceof byte[])
                data = new String((byte[]) data);
            if (data instanceof String)
                return JsonUtils.read(
                        (String) data,
                        new TypeObject<ModuleConfig>() {
                        }
                );
            if (data instanceof Map)
                return JsonUtils.convert(
                        data,
                        new TypeObject<ModuleConfig>() {
                        }
                );
            throw new Exception("Invalid source: " + source);
        } catch (Exception e) {
            logger.error("Parse config failed.", e);
            throw new ConfigParseException("Config parse failed.");
        }
    }

}
