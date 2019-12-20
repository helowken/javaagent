package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.base.utils.MethodDescriptorUtils;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.MethodFinder;
import agent.server.transform.TransformContext;
import agent.server.transform.exception.InvalidTransformerConfigException;

import java.lang.reflect.Method;
import java.util.Collection;
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

    protected TransformerInfo getTransformerInfo() {
        return transformerInfo;
    }

    protected String getContext() {
        return getTransformerInfo().getContext();
    }

    @Override
    public void transform(TransformContext transformContext) throws Exception {
        for (Class<?> clazz : transformContext.getTargetClassSet()) {
            Collection<Method> methods = MethodFinder.getInstance().find(
                    transformerInfo.getTargetClassConfig(
                            clazz.getName()
                    )
            ).methods;

            for (Method method : methods) {
                logger.debug("Transforming method: {}", MethodDescriptorUtils.getLongName(method));
                transformMethod(method);
            }
        }
    }

    protected abstract void transformMethod(Method method) throws Exception;
}
