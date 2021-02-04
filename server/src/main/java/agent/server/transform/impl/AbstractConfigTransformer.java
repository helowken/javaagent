package agent.server.transform.impl;

import agent.invoke.DestInvoke;
import agent.server.transform.ConfigTransformer;
import agent.server.transform.TransformContext;
import agent.server.transform.TransformerData;
import agent.server.transform.exception.InvalidTransformerConfigException;
import agent.server.utils.log.LogMgr;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractConfigTransformer extends AbstractTransformer implements ConfigTransformer {
    private static final String KEY_LOG = "log";
    private String tid;
    private TransformerData transformerData = new TransformerData();
    private Map<String, Object> config;

    @Override
    public void setTid(String tid) {
        this.tid = tid;
    }

    @Override
    public String getTid() {
        return tid;
    }

    @Override
    public TransformerData getTransformerData() {
        return transformerData;
    }

    @Override
    public void setConfig(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public void init() {
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
        LogMgr.close(
                getTid()
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLogConfig(Map<String, Object> config) {
        return (Map) config.getOrDefault(
                KEY_LOG,
                Collections.emptyMap()
        );
    }

    protected void regLog(Map<String, Object> config, Map<String, Object> overwrite) {
        Map<String, Object> logConfig = new HashMap<>(
                getLogConfig(config)
        );
        if (overwrite != null)
            logConfig.putAll(overwrite);
        LogMgr.regBinary(
                getTid(),
                logConfig,
                Collections.emptyMap()
        );
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
