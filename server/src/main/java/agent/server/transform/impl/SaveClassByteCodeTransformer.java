package agent.server.transform.impl;

import agent.base.utils.IOUtils;

import java.io.File;
import java.security.ProtectionDomain;

public class SaveClassByteCodeTransformer extends AbstractTransformer {
    private static final String dir = "/tmp/javaagent/classes/";
    private static final String CLASS_FILE_SUFFIX = ".class";

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                 ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
        String parentDir = null;
        String fileName = className + CLASS_FILE_SUFFIX;
        int pos = fileName.lastIndexOf("/");
        if (pos > -1) {
            parentDir = dir + fileName.substring(0, pos);
            new File(parentDir).mkdirs();
            fileName = fileName.substring(pos + 1);
        }
        System.out.println("==============: " + new File(parentDir, fileName).getAbsolutePath());
        IOUtils.writeBytes(
                new File(parentDir, fileName).getAbsolutePath(),
                classfileBuffer,
                false
        );
        return classfileBuffer;
    }

    @Override
    protected boolean accept(ClassLoader loader, String namePath) {
        return true;
    }
}
