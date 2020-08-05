package agent.common.args.parse;

import agent.base.utils.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

abstract class AbstractOptParser implements OptParser {
    private static final Logger logger = Logger.getLogger(AbstractOptParser.class);
    private final List<OptConfig> optConfigList = new ArrayList<>();

    AbstractOptParser(OptConfig... optConfigs) {
        if (optConfigs == null || optConfigs.length == 0)
            throw new IllegalArgumentException();
        add(optConfigs);
    }

    abstract Object getValue(String arg, ArgList argList, OptConfig optConfig);

    private void add(OptConfig[] optConfigs) {
        Set<String> nameSet = new HashSet<>();
        Set<String> fullNameSet = new HashSet<>();
        Set<String> keySet = new HashSet<>();
        for (OptConfig optConfig : optConfigs) {
            String name = optConfig.getName();
            if (name != null) {
                if (nameSet.contains(name))
                    throw new RuntimeException("Duplicated name: " + name);
                else
                    nameSet.add(name);
            }

            String fullName = optConfig.getFullName();
            if (fullName != null) {
                if (fullNameSet.contains(fullName))
                    throw new RuntimeException("Duplicated full name: " + fullName);
                else
                    fullNameSet.add(fullName);
            }

            String key = optConfig.getKey();
            if (keySet.contains(key))
                throw new RuntimeException("Duplicated key: " + key);
            else
                keySet.add(key);

            optConfigList.add(optConfig);
        }
    }

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
