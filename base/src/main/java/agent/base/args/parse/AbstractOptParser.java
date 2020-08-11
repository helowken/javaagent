package agent.base.args.parse;

import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.List;

abstract class AbstractOptParser implements OptParser {
    private static final Logger logger = Logger.getLogger(AbstractOptParser.class);
    private final List<OptConfig> optConfigList = new ArrayList<>();

    AbstractOptParser(Object... vs) {
        if (vs == null || vs.length == 0)
            throw new IllegalArgumentException();
        List<OptConfig> optConfigs = new ArrayList<>();
        for (Object v : vs) {
            if (v instanceof OptConfig)
                optConfigs.add((OptConfig) v);
            else if (v instanceof OptConfigSuite)
                optConfigs.addAll(((OptConfigSuite) v).getOptConfigList());
            else
                throw new IllegalArgumentException("Invalid param: " + v);
        }
        OptConfigSuite.validate(optConfigs);
        optConfigList.addAll(optConfigs);
    }

    abstract Object getValue(String arg, ArgList argList, OptConfig optConfig);

    @Override
    public boolean parse(String arg, ArgList argList, Opts opts) {
        for (OptConfig optConfig : optConfigList) {
            if (optConfig.match(arg)) {
                checkDuplicated(arg, optConfig, opts);
                opts.put(
                        optConfig.getKey(),
                        getValue(arg, argList, optConfig)
                );
                return true;
            }
        }
        return false;
    }

    private void checkDuplicated(String arg, OptConfig optConfig, Opts opts) {
        if (opts.contains(optConfig.getKey()) && !optConfig.isAllowMulti())
            throw new RuntimeException("Duplicated option: " + arg);
    }

    Object convertValue(String arg, String valueStr, OptConfig optConfig) {
        try {
            switch (optConfig.getValueType()) {
                case INT:
                    return Integer.parseInt(valueStr);
                case STRING:
                    return valueStr;
            }
        } catch (Exception e) {
            logger.error("Convert value failed, arg: {}, valueStr: {}", e, arg, valueStr);
        }
        throw new RuntimeException("Invalid value for " + arg + ": " + valueStr);
    }
}
