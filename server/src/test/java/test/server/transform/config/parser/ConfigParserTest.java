package test.server.transform.config.parser;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.server.transform.config.*;
import agent.server.transform.config.parser.FileConfigParser;
import agent.server.transform.config.parser.FileConfigParser.FileConfigItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import test.server.AbstractTest;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ConfigParserTest extends AbstractTest {
    private static final Logger logger = Logger.getLogger(ConfigParserTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final FileConfigParser fileConfigParser = new FileConfigParser();

    @Test
    public void testParseFileConfig() throws Exception {
        ModuleConfig moduleConfig = createModuleConfig();
        String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(moduleConfig);
        logger.info("Content: \n{}", content);

        ModuleConfig parsedConfig = fileConfigParser.parse(new FileConfigItem(content));
        assertEquals(moduleConfig, parsedConfig);

        parsedConfig = fileConfigParser.parse(new FileConfigItem(content.getBytes()));
        assertEquals(moduleConfig, parsedConfig);

        File file = File.createTempFile("config", ".json");
        IOUtils.writeString(file.getAbsolutePath(), content, false);
        parsedConfig = fileConfigParser.parse(new FileConfigItem(file));
        assertEquals(moduleConfig, parsedConfig);
    }

    private ModuleConfig createModuleConfig() {
        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfig.setContextPath("/test");

        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setRef("timeMeasure");
        Map<String, Object> config = new HashMap<>();
        config.put("outputFile", "/tmp/111/222");
        config.put("outputFormat", "Cost time: $costTime$");
        transformerConfig.setConfig(config);

        TargetConfig targetConfig = new TargetConfig();
        ClassFilterConfig classFilterConfig = new ClassFilterConfig();
        classFilterConfig.setIncludes(
                Collections.singleton("test.jetty.TestObject")
        );
        targetConfig.setClassFilter(classFilterConfig);
        MethodFilterConfig methodConfig = new MethodFilterConfig();
        methodConfig.setIncludes(Collections.singleton("test"));
        targetConfig.setMethodFilter(methodConfig);
        moduleConfig.setTargets(Collections.singletonList(targetConfig));

        return moduleConfig;
    }
}
