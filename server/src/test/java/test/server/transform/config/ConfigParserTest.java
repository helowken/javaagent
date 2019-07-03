package test.server.transform.config;

import agent.base.utils.IOUtils;
import agent.base.utils.Logger;
import agent.server.transform.config.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.File;
import java.util.*;

import static org.junit.Assert.assertEquals;

public class ConfigParserTest {
    private static final Logger logger = Logger.getLogger(ConfigParserTest.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testParseConfig() throws Exception {
        List<ModuleConfig> moduleConfigList = createModuleConfigList();
        String content = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(moduleConfigList);
        logger.info("Content: \n{}", content);

        List<ModuleConfig> parsedList = ConfigParser.parse(content);
        assertEquals(moduleConfigList, parsedList);

        parsedList = ConfigParser.parse(content.getBytes());
        assertEquals(moduleConfigList, parsedList);

        File file = File.createTempFile("config", ".json");
        IOUtils.writeString(file.getAbsolutePath(), content, false);
        parsedList = ConfigParser.parse(file);
        assertEquals(moduleConfigList, parsedList);
    }

    private List<ModuleConfig> createModuleConfigList() {
        List<ModuleConfig> moduleConfigList = new ArrayList<>();
        ModuleConfig moduleConfig = new ModuleConfig();
        moduleConfigList.add(moduleConfig);
        moduleConfig.setContextPath("/test");

        TransformConfig transformConfig = new TransformConfig();
        transformConfig.setDesc("Used to measure time cost.");
        moduleConfig.setTransformConfigList(Collections.singletonList(transformConfig));

        TransformerConfig transformerConfig = new TransformerConfig();
        transformerConfig.setRef("timeMeasure");
        Map<String, Object> config = new HashMap<>();
        config.put("outputFile", "/tmp/111/222");
        config.put("outputFormat", "Cost time: $costTime$");
        transformerConfig.setConfig(config);
        transformConfig.setTransformerConfigList(Collections.singletonList(transformerConfig));

        ClassConfig classConfig = new ClassConfig();
        classConfig.setTargetClass("test.jetty.TestObject");
        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setName("test");
        classConfig.setMethodConfigList(Collections.singletonList(methodConfig));
        transformConfig.setTargetList(Collections.singletonList(classConfig));

        return moduleConfigList;
    }
}
