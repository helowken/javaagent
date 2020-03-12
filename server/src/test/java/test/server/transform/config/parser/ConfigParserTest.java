package test.server.transform.config.parser;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.common.config.*;
import agent.server.transform.config.parser.ConfigParser;
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

    @Test
    public void testParseFileConfig() throws Exception {
        ModuleConfig moduleConfig = createModuleConfig();
        Map<String, Object> map = objectMapper.convertValue(moduleConfig, Map.class);
        ModuleConfig parsedConfig = ConfigParser.parse(map);
        assertEquals(moduleConfig, parsedConfig);

        String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(moduleConfig);
        logger.info("Content: \n{}", content);

        parsedConfig = ConfigParser.parse(content);
        assertEquals(moduleConfig, parsedConfig);

        parsedConfig = ConfigParser.parse(content.getBytes());
        assertEquals(moduleConfig, parsedConfig);

        File file = File.createTempFile("config", ".json");
        IOUtils.writeString(file.getAbsolutePath(), content, false);
        parsedConfig = ConfigParser.parse(file);
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
        moduleConfig.setTransformers(
                Collections.singletonList(transformerConfig)
        );

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
