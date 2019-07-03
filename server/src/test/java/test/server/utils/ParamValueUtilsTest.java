package test.server.utils;

import agent.base.utils.Logger;
import agent.server.utils.ParamValueUtils;
import org.junit.Test;

public class ParamValueUtilsTest {
    private static final Logger logger = Logger.getLogger(ParamValueUtilsTest.class);

    @Test
    public void test() {
        logger.info("Content: \n{}",
                ParamValueUtils.genCode("test.jetty.TestObject", "test", "costTime", "etVar - stVar"));
    }
}
