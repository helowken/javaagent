package agent.server.transform.impl.dynamic;

import agent.base.utils.Pair;
import agent.server.transform.ClassDataFinder;
import agent.server.transform.impl.AbstractTransformer;
import agent.server.transform.impl.TransformerInfo;

import java.security.ProtectionDomain;
import java.util.Map;
import java.util.Optional;

public class AdditionalClassTransformer extends AbstractTransformer {
    private final Map<String, Pair<ClassLoader, byte[]>> classNameToPair;

    public AdditionalClassTransformer(Map<String, Pair<ClassLoader, byte[]>> classNameToPair) {
        this.classNameToPair = classNameToPair;
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
        byte[] data = Optional.ofNullable(
                classNameToPair.get(targetClassName).right
        ).orElse(classfileBuffer);
//        ClassDataFinder.getInstance().updateClassData(classBeingRedefined, data);
        return data;
    }

    @Override
    protected boolean accept(ClassLoader loader, String namePath) {
        return Optional.ofNullable(
                classNameToPair.get(
                        TransformerInfo.getClassName(namePath)
                )
        ).map(p -> p.left.equals(loader))
                .orElse(false);
    }

}
