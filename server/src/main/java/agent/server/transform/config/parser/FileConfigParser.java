package agent.server.transform.config.parser;

import agent.base.utils.IOUtils;
import agent.base.utils.TypeObject;
import agent.common.utils.JSONUtils;
import agent.server.transform.config.ModuleConfig;
import agent.server.transform.config.parser.exception.ConfigParseException;

import java.io.File;
import java.util.List;

public class FileConfigParser implements ConfigParser {
    @Override
    public List<ModuleConfig> parse(ConfigItem item) throws ConfigParseException {
        try {
            return JSONUtils.read(
                    getContent(
                            ((FileConfigItem) item).source
                    ),
                    new TypeObject<List<ModuleConfig>>() {
                    }
            );
        } catch (Exception e) {
            throw new ConfigParseException("Config parse failed: " + item, e);
        }
    }

    @Override
    public ConfigParserType getType() {
        return ConfigParserType.FILE;
    }

    private String getContent(Object source) throws Exception {
        Object v = source;
        if (v instanceof File)
            v = IOUtils.readBytes(((File) v).getAbsolutePath());
        if (v instanceof byte[])
            v = new String((byte[]) v);
        if (v instanceof String)
            return (String) v;
        throw new Exception("Invalid source: " + source);
    }

    public static class FileConfigItem implements ConfigItem {
        private final Object source;

        public FileConfigItem(Object source) {
            this.source = source;
        }

        @Override
        public ConfigParserType getType() {
            return ConfigParserType.FILE;
        }
    }
}
