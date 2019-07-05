package agent.hook.utils;

import java.net.URL;
import java.net.URLClassLoader;

public class AgentClassLoader extends URLClassLoader {
    private ClassLoader parent;

    public AgentClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
}
