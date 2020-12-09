package agent.server.transform.impl;

import agent.invoke.DestInvoke;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformerData;
import agent.server.transform.exception.InvalidTransformerConfigException;
import agent.server.utils.log.LogConfig;
import agent.server.utils.log.LogMgr;
import agent.server.utils.log.LoggerType;

import java.util.Collections;
import java.util.Map;

public abstract class AbstractConfigTransformer extends AbstractTransformer implements ConfigTransformer {
    private String tid;
    private TransformerData transformerData;

    @Override
    public void setTid(String tid) {
        this.tid = tid;
    }
    
    @Override
    public String getTid() {
        return tid;
    }
   
    @Override
    public void setTransformerData(TransformerData transformerData) {
        this.transformerData = transformerData;
    }
    
    @Override 
    public TransformerData getTransformerData() {
        return transformerData;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        try {
            doSetConfig(config == null ? Collections.emptyMap() : config);
        } catch (InvalidTransformerConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidTransformerConfigException("Set config failed: " + config, e);
        }
    }

    @Override
    public void destroy() {
        transformerData.clear();
    }

    protected String regLogBinary(Map<String, Object> config, Map<String, Object> defaultValueMap) {
        return regLog(LoggerType.BINARY, config, defaultValueMap);
    }

    private String regLog(LoggerType loggerType, Map<String, Object> config, Map<String, Object> defaultValueMap) {
        String logKey = LogMgr.reg(loggerType, config, defaultValueMap);
        LogConfig logConfig = LogMgr.getLogConfig(loggerType, logKey);
        DestInvokeIdRegistry.getInstance().regOutputPath(
                logConfig.getOutputPath()
        );
        return logKey;
    }

    protected void doSetConfig(Map<String, Object> config) throws Exception {
    }

    @Override
    public void transform(TransformContext transformContext) throws Exception {
        for (DestInvoke invoke : transformContext.getInvokeSet()) {
            transformDestInvoke(invoke);
        }
    }


    protected abstract void transformDestInvoke(DestInvoke destInvoke) throws Exception;
}
