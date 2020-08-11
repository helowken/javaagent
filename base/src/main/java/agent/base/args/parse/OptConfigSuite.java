package agent.base.args.parse;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptConfigSuite {
    private final Map<String, OptConfig> keyToOptConfig;

    public OptConfigSuite(OptConfig... optConfigs) {
        validate(optConfigs);
        keyToOptConfig = Stream.of(optConfigs).collect(
                Collectors.toMap(
                        OptConfig::getKey,
                        v -> v
                )
        );
    }

    public OptConfig findByKey(String key) {
        OptConfig optConfig = keyToOptConfig.get(key);
        if (optConfig == null)
            throw new RuntimeException("No option config found by key: " + key);
        return optConfig;
    }

    public <T> T get(Opts opts, String key, Predicate<T> predicate) {
        T v = opts.get(key);
        if (!predicate.test(v)) {
            OptConfig optConfig = findByKey(key);
            throw new RuntimeException(
                    "Option '" + optConfig.getDisplayName() + "' is required."
            );
        }
        return v;
    }

    Collection<OptConfig> getOptConfigList() {
        return Collections.unmodifiableCollection(
                keyToOptConfig.values()
        );
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
