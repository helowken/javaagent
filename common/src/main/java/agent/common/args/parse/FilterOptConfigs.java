package agent.common.args.parse;

import agent.base.args.parse.OptConfig;
import agent.base.args.parse.OptConfigSuite;
import agent.base.args.parse.OptValueType;
import agent.base.args.parse.Opts;
import agent.base.utils.Utils;

import java.util.Collections;
import java.util.List;

public class FilterOptConfigs {
    private static final String KEY_MATCH_FILTER = "MATCH_FILTER";

    private static final OptConfigSuite suite = new OptConfigSuite(
            new OptConfig(
                    "-f",
                    "--match-filter",
                    KEY_MATCH_FILTER,
                    "Filter rules for classes, methods and constructors",
                    OptValueType.STRING,
                    true
            )
    );

    public static OptConfigSuite getSuite() {
        return suite;
    }

    public static List<FilterItem> getFilterList(Opts opts) {
        List<Object> filterStrs = opts.getList(KEY_MATCH_FILTER);
        return filterStrs == null ?
                Collections.emptyList() :
                FilterOptUtils.parse(filterStrs);
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
