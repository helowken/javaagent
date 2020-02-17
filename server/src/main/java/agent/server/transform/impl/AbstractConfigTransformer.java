package agent.server.transform.impl;

import agent.base.utils.ClassLoaderUtils;
import agent.base.utils.Logger;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.InvokeFinder;
import agent.server.transform.TransformContext;
import agent.server.transform.exception.InvalidTransformerConfigException;
import agent.server.transform.impl.invoke.DestInvoke;
import agent.server.utils.log.LogConfig;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.LoggerType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static agent.hook.utils.App.getClassFinder;

@SuppressWarnings("unchecked")
public abstract class AbstractConfigTransformer extends AbstractTransformer implements ConfigTransformer {
    private static final Logger logger = Logger.getLogger(AbstractConfigTransformer.class);
    private TransformShareInfo transformShareInfo;

    @Override
    public void setTransformerInfo(TransformShareInfo transformShareInfo) {
        this.transformShareInfo = transformShareInfo;
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
        DestInvokeIdRegistry.getInstance().regOutputPath(
                getContext(),
                logConfig.getOutputPath()
        );
        return logKey;
    }

    protected void doSetConfig(Map<String, Object> config) throws Exception {
    }

    protected TransformShareInfo getTransformerInfo() {
        return transformShareInfo;
    }

    protected String getContext() {
        return getTransformerInfo().getContext();
    }

    protected <T> Class<T> findClass(String className) {
        return (Class<T>) getClassFinder().findClass(
                getContext(),
                className
        );
    }

    @Override
    public void transform(TransformContext transformContext) throws Exception {
        for (Class<?> clazz : transformContext.getTargetClassSet()) {
            Collection<DestInvoke> invokes = InvokeFinder.getInstance().find(
                    clazz,
                    transformShareInfo.getInvokeFilters(clazz)
            ).invokes;

            for (DestInvoke invoke : invokes) {
                logger.debug("Transforming invoke: {}", invoke);
                DestInvokeIdRegistry.getInstance().reg(
                        findContextOfDestInvoke(invoke),
                        invoke
                );
                transformDestInvoke(invoke);
            }
        }
    }

    private String findContextOfDestInvoke(DestInvoke destInvoke) {
        String context = getContext();
        ClassLoader contextLoader = getClassFinder().findClassLoader(context);
        return ClassLoaderUtils.isSelfOrDescendant(
                contextLoader,
                destInvoke.getDeclaringClass().getClassLoader()
        ) ?
                context :
                null;
    }

    protected abstract void transformDestInvoke(DestInvoke destInvoke) throws Exception;
}
