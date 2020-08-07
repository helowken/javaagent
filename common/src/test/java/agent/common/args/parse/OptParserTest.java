package agent.common.args.parse;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OptParserTest {
    @Test
    public void test() {
        OptParser kvParser = new KeyValueOptParser(
                new OptConfig("-o", "key1"),
                new OptConfig(null, "--value", "twoValues", OptionValueType.STRING, true)
        );
        OptParser singleParser = new BooleanOptParser(
                new OptConfig("-v", "sv"),
                new OptConfig("-p", "sp"),
                new OptConfig("-t", "st")
        );

        String[] args = {
                "param0",
                "-o", "111",
                "--value", "v1",
                "param1",
                "-v",
                "-p",
                "--value", "v2",
                "param2",
                "-t"
        };

        ArgsOptsParser parser = new ArgsOptsParser(
                kvParser,
                singleParser
        );
        ArgsOpts rs = parser.parse(args);
        final int totalOptSize = 5;
        checkOpts(rs, totalOptSize, "key1", "111", 1);
        checkOpts(rs, totalOptSize, "twoValues", Arrays.asList("v1", "v2"), 2);
        checkOpts(rs, totalOptSize, "sv", true, 1);
        checkOpts(rs, totalOptSize, "sp", true, 1);
        checkOpts(rs, totalOptSize, "st", true, 1);
        checkArgs(rs, 3, new String[]{"param0", "param1", "param2"});

        checkFail(new String[]{"-ov", "111"}, parser, "Unknown option: -ov");
        checkFail(new String[]{"--value2", "111"}, parser, "Unknown option: --value2");
        checkFail(new String[]{"-y"}, parser, "Unknown option: -y");
        checkFail(new String[]{"-v", "-v"}, parser, "Duplicated option: -v");
        checkFail(new String[]{"-o", "111", "-o", "222"}, parser, "Duplicated option: -o");
        checkFail(new String[]{"-o", "-v"}, parser, "No option value found for: -o");
    }

    private void checkFail(String[] args, ArgsOptsParser parser, String errMsg) {
        try {
            parser.parse(args);
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), errMsg);
        }
    }

    private void checkArgs(ArgsOpts rs, int totalSize, String[] values) {
        assertEquals(totalSize, rs.argSize());
        assertEquals(Arrays.asList(values), Arrays.asList(rs.getArgs()));
    }

    private void checkOpts(ArgsOpts rs, int totalSize, String key, Object values, int optSize) {
        assertEquals(totalSize, rs.optSize());
        assertEquals(optSize, rs.sizeOfOpt(key));
        if (optSize == 1)
            assertEquals(values, rs.getOptValue(key));
        else
            assertEquals(values, rs.getOptValues(key));
    }
}