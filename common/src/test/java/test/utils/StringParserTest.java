package test.utils;

import org.junit.Test;
import agent.base.exception.StringParseException;
import agent.base.utils.StringParser;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class StringParserTest {

    @Test
    public void test() {
        String a0 = "aa";
        int a1 = 333;
        float a2 = 2.5F;
        boolean a3 = true;
        boolean a4 = false;
        Map<String, Object> pvs = new HashMap<>();
        pvs.put("a0", a0);
        pvs.put("a1", a1);
        pvs.put("a2", a2);
        pvs.put("a3", a3);
        pvs.put("a4", a4);

        check(a0, "${a0}", pvs);
        check(a0 + "${", "${a0}${", pvs);
        check(a0 + "}", "${a0}}", pvs);
        check(a0 + a1, "${a0}${a1}", pvs);
        check(a0 + "-${aaa", "${a0}-${aaa", pvs);
        check(a0 + "-" + a1 + " " + a2, "${a0}-${a1} ${a2}", pvs);
        check("aa" + a3 + " bb}}}", "aa${a3} bb}}}", pvs);
        check("$ {a}a}bb" + a4 + "}}" + a4 + "${${", "$ {a}a}bb${a4}}}${a4}${${", pvs);

        checkFail("${${a1}", pvs, "${a1");
        checkFail("${a999}", pvs, "a999");
        checkFail("${}", pvs, "");
        checkFail("${   }", pvs, "");
        checkFail("${ }", pvs, "");
    }

    private void checkFail(String pattern, Map<String, Object> pvs, String errKey) {
        try {
            StringParser.eval(pattern, pvs);
            fail();
        } catch (StringParseException e) {
            assertEquals("No value found by key: \"" + errKey + "\"", e.getMessage());
        }
    }

    private void check(Object expectedValue, String pattern, Map<String, Object> pvs) {
        assertEquals(expectedValue, StringParser.eval(pattern, pvs));
    }
}
