package agent.server.transform.config;

import com.fasterxml.jackson.core.type.TypeReference;
import agent.base.utils.IOUtils;
import agent.common.utils.JSONUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConfigParser {
    public static List<ModuleConfig> parse(File file) throws IOException {
        return parse(IOUtils.readBytes(file.getAbsolutePath()));
    }

    public static List<ModuleConfig> parse(byte[] bs) throws IOException {
        return parse(new String(bs));
    }

    public static List<ModuleConfig> parse(String content) throws IOException {
        return JSONUtils.read(content, new TypeReference<List<ModuleConfig>>() {
        });
    }

}
