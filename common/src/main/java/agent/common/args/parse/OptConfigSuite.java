package agent.common.args.parse;

import java.util.*;

public class OptConfigSuite {
    private final List<OptConfig> optConfigList = new ArrayList<>();

    public OptConfigSuite(OptConfig... optConfigs) {
        validate(optConfigs);
        Collections.addAll(optConfigList, optConfigs);
    }

    List<OptConfig> getOptConfigList() {
        return Collections.unmodifiableList(optConfigList);
    }

    public static void validate(OptConfig... optConfigs) {
        if (optConfigs == null || optConfigs.length == 0)
            throw new IllegalArgumentException();
        validate(
                Arrays.asList(optConfigs)
        );
    }

    public static void validate(Collection<OptConfig> optConfigs) {
        if (optConfigs.isEmpty())
            throw new IllegalArgumentException();
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
        }
    }
}
