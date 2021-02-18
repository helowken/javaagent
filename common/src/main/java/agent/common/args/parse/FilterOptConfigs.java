package agent.common.args.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.OptValueType;
import agent.base.args.parse.Opts;
import agent.base.utils.FileUtils;
import agent.base.utils.IOUtils;
import agent.base.utils.Utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FilterOptConfigs {
    private static final String KEY_MATCH_FILTER = "MATCH_FILTER";
    private static final String KEY_FILTER_FILE = "FILTER_FILE";

    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-f",
                    "--match-filter",
                    KEY_MATCH_FILTER,
                    "Filter rules for classes, methods and constructors.",
                    OptValueType.STRING,
                    true
            ),
            new OptConfig(
                    "-ff",
                    "--filter-file",
                    KEY_FILTER_FILE,
                    "A file contains filter rules.",
                    OptValueType.STRING,
                    true
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    @SuppressWarnings("unchecked")
    static List<FilterItem> getFilterList(Opts opts) {
        List<String> filterStrs = (List) opts.getList(KEY_MATCH_FILTER);
        Set<String> rules = new HashSet<>(
                getFilterRulesFromFile(opts)
        );
        if (filterStrs != null)
            rules.addAll(filterStrs);
        return FilterOptUtils.parse(rules);
    }

    private static List<String> getFilterRulesFromFile(Opts opts) {
        return Utils.wrapToRtError(
                () -> {
                    String filterFilePath = opts.get(KEY_FILTER_FILE);
                    if (Utils.isNotBlank(filterFilePath)) {
                        String filePath = FileUtils.getAbsolutePath(filterFilePath, true);
                        List<String> rows = IOUtils.readRows(filePath, Utils::isNotBlank);
                        if (rows != null && !rows.isEmpty())
                            return rows;
                        throw new RuntimeException("No filter rules in file: " + filterFilePath);
                    }
                    return Collections.emptyList();
                }
        );
    }

    public static void checkClassFilter(Opts opts) {
        List<FilterItem> matchFilterList = FilterOptConfigs.getFilterList(opts);
        for (FilterItem matchFilter : matchFilterList) {
            if (Utils.isNotBlank(matchFilter.classStr))
                return;
        }
        throw new RuntimeException("No class filter specified.");
    }
}
