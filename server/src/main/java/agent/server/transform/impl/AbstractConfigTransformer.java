package agent.server.transform.impl;

import agent.base.utils.Logger;
import agent.base.utils.MethodDescriptorUtils;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.MethodFinder;
import agent.server.transform.TransformContext;
import agent.server.transform.exception.InvalidTransformerConfigException;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.transform.impl.invoke.MethodInvoke;
import agent.server.utils.log.LogConfig;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.LoggerType;

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

    protected String regLogText(Map<String, Object> config, Map<String, Object> defaultValueMap) {
        return regLog(LoggerType.TEXT, config, defaultValueMap);
    }

    protected String regLogBinary(Map<String, Object> config, Map<String, Object> defaultValueMap) {
        return regLog(LoggerType.BINARY, config, defaultValueMap);
    }

    private String regLog(LoggerType loggerType, Map<String, Object> config, Map<String, Object> defaultValueMap) {
        String logKey = LogMgr.reg(loggerType, config, defaultValueMap);
        LogConfig logConfig = LogMgr.getLogConfig(loggerType, logKey);
        if (!logConfig.isStdout())
            DestInvokeIdRegistry.getInstance().regOutputPath(
                    getContext(),
                    logConfig.getOutputPath()
            );
        return logKey;
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
                DestInvoke destInvoke = new MethodInvoke(method);
                DestInvokeIdRegistry.getInstance().reg(destInvoke);
                transformDestInvoke(destInvoke);
            }
        }
    }

    protected abstract void transformDestInvoke(DestInvoke destInvoke) throws Exception;
}
