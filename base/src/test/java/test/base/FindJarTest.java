package test.base;

import agent.base.utils.JavaToolUtils;
import org.junit.Test;

public class FindJarTest {
    @Test
    public void test() throws Exception {
        JavaToolUtils.findJarByClassNames(
                "/home/helowken/test_tomcat/apache-tomcat-7.0.94/lib",
//                "org/apache/tomcat/util/digester/Rule.class"
                "org.apache.jasper.compiler.JspRuntimeContext"
        ).forEach((k, v) -> System.out.println(k + ": " + v));
    }
}
