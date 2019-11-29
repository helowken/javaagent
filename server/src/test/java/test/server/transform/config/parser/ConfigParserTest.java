package test.server.transform.config.parser;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.server.transform.config.*;
import agent.server.transform.config.parser.FileConfigParser;
import agent.server.transform.config.parser.FileConfigParser.FileConfigItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class ConfigParserTest {
    private static final Logger logger = Logger.getLogger(ConfigParserTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final FileConfigParser fileConfigParser = new FileConfigParser();

    @Test
    public void testParseFileConfig() throws Exception {
        List<ModuleConfig> moduleConfigList = createModuleConfigList();
        String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(moduleConfigList);
        logger.info("Content: \n{}", content);

        List<ModuleConfig> parsedList = fileConfigParser.parse(new FileConfigItem(content));
        assertEquals(moduleConfigList, parsedList);

        parsedList = fileConfigParser.parse(new FileConfigItem(content.getBytes()));
        assertEquals(moduleConfigList, parsedList);

        File file = File.createTempFile("config", ".json");
        IOUtils.writeString(file.getAbsolutePath(), content, false);
        parsedList = fileConfigParser.parse(new FileConfigItem(file));
        assertEquals(moduleConfigList, parsedList);
    }

    private List<ModuleConfig> createModuleConfigList() {
        List<ModuleConfig> moduleConfigList = new ArrayList<>();
        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfigList.add(moduleConfig);
        moduleConfig.setContextPath("/test");

        TransformConfig transformConfig = new TransformConfig();
        transformConfig.setDesc("Used to measure time cost.");
        moduleConfig.setTransformConfigs(Collections.singletonList(transformConfig));

        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setRef("timeMeasure");
        Map<String, Object> config = new HashMap<>();
        config.put("outputFile", "/tmp/111/222");
        config.put("outputFormat", "Cost time: $costTime$");
        transformerConfig.setConfig(config);
        transformConfig.setTransformers(Collections.singletonList(transformerConfig));

        ClassConfig classConfig = new ClassConfig();
        classConfig.setTargetClass("test.jetty.TestObject");
        MethodFilterConfig methodConfig = new MethodFilterConfig();
        methodConfig.setIncludes(Collections.singleton("test"));
        classConfig.setMethodFilter(methodConfig);
        transformConfig.setTargets(Collections.singletonList(classConfig));

        return moduleConfigList;
    }
}
