package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.base.utils.MethodSignatureUtils;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.exception.InvalidTransformerConfigException;
import agent.server.transform.impl.utils.AgentClassPool;
import agent.server.transform.impl.utils.MethodFinder;
import agent.server.transform.impl.utils.MethodFinder.MethodSearchResult;
import javassist.CannotCompileException;
import javassist.CtClass;

import java.io.IOException;
import java.lang.reflect.Method;
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

    protected void doSetConfig(Map<String, Object> config) throws Exception {
    }

    protected boolean accept(ClassLoader loader, String namePath) {
        return transformerInfo.accept(loader, namePath);
    }

    protected String getTargetClassName(String namePath) {
        return transformerInfo.getTargetClassName(namePath);
    }

    protected TransformerInfo getTransformerInfo() {
        return transformerInfo;
    }

    @Override
    protected byte[] doTransform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                 ProtectionDomain protectionDomain, byte[] classfileBuffer, String targetClassName) throws Exception {
        MethodSearchResult rs = MethodFinder.getInstance().find(
                transformerInfo.getTargetClassConfig(className)
        );
        for (Method method : rs.methods) {
            logger.debug("Transforming method: {}", MethodSignatureUtils.getLongName(method));
            transformMethod(method);
        }
        TransformSession.get().addTransformClass(
                transformerInfo.getContext(),
                rs.clazz,
                getClassData(rs.clazz)
        );
        return classfileBuffer;
    }

    protected byte[] getClassData(Class<?> clazz) throws IOException, CannotCompileException {
        return AgentClassPool.getInstance().get(clazz).toBytecode();
    }

    protected abstract void transformMethod(Method method) throws Exception;
}
