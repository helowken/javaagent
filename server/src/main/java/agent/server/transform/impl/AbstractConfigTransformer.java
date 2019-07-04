package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.exception.InvalidTransformerConfigException;
import javassist.CtClass;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Map;

@SuppressWarnings("unchecked")
public abstract class AbstractConfigTransformer extends AbstractTransformer implements ConfigTransformer {
    private static final Logger logger = Logger.getLogger(AbstractConfigTransformer.class);
    private TransformerInfo transformerInfo;

    @Override
    public void setTransformerInfo(TransformerInfo transformerInfo) {
        this.transformerInfo = transformerInfo;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        try {
            doSetConfig(config == null ? Collections.emptyMap() : config);
        } catch (InvalidTransformerConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTransformerConfigException("Set config failed.", e);
        }
    }

    protected void doSetConfig(Map<String, Object> config) {
    }

    protected boolean accept(ClassLoader loader, String namePath) {
        return transformerInfo.accept(loader, namePath);
    }

    protected String getTargetClassName(String namePath) {
        return transformerInfo.getTargetClassName(namePath);
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                 ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
        MethodFinder.MethodSearchResult rs = MethodFinder.getInstance().rawFind(transformerInfo.getTargetClassConfig(className));
        CtClass ctClass = rs.ctClass;
        for (CtMethod ctMethod : rs.methodList) {
            logger.info("Transforming method: {}", ctMethod.getLongName());
            transformMethod(ctClass, ctMethod);
        }
        byte[] byteCode = ctClass.toBytecode();
        ctClass.detach();
        return byteCode;
    }

    protected abstract void transformMethod(CtClass ctClass, CtMethod ctMethod) throws Exception;
}
