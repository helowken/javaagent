package agent.base.help;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static agent.base.help.HelpSection.PADDING_1;

public class HelpArg {
    private final String name;
    private final String desc;
    private final boolean isOpt;
    private final List<HelpArgValue> values = new ArrayList<>();

    public HelpArg(String name, String desc) {
        this(name, desc, false);
    }

    public HelpArg(String name, String desc, boolean isOptional) {
        this.name = name;
        this.desc = desc;
        this.isOpt = isOptional;
    }

    public boolean isOptional() {
        return isOpt;
    }

    public String getName() {
        return name;
    }

    public String getUsageName() {
        return isOpt ? "[<" + name + ">]" : "<" + name + ">";
    }

    public HelpArg add(String value, String desc, boolean isDefault) {
        values.add(
                new HelpArgValue(value, desc, isDefault)
        );
        return this;
    }

    public HelpArg add(String value, String desc) {
        return this.add(value, desc, false);
    }

    private String getTitle() {
        String title = name + ": " + desc;
        for (HelpArgValue argValue : values) {
            if (argValue.isDef) {
                if (!title.endsWith("."))
                    title += ".";
                return title + " Default is " + argValue.value;
            }
        }
        return title;
    }

    public HelpInfo getHelp() {
        String title = getTitle();
        if (!values.isEmpty()) {
            return new HelpSection(title, PADDING_1)
                    .add(
                            values.stream()
                                    .map(
                                            argValue -> new HelpKeyValue(argValue.value, argValue.desc)
                                    )
                                    .collect(
                                            Collectors.toList()
                                    )
                    );
        }
        return new HelpSingleValue("\n" + title);
    }

    private static class HelpArgValue {
        final String value;
        final String desc;
        final boolean isDef;

        HelpArgValue(String value, String desc, boolean isDef) {
            this.value = value;
            this.desc = desc;
            this.isDef = isDef;
        }
    }
}
