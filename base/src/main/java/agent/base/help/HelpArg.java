package agent.base.help;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static agent.base.help.HelpSection.PADDING_2;

public class HelpArg {
    private final String name;
    private final String desc;
    private final List<HelpArgValue> values = new ArrayList<>();

    public HelpArg(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    public void add(String value, String desc, boolean isDefault) {
        values.add(
                new HelpArgValue(value, desc, isDefault)
        );
    }

    public void add(String value, String desc) {
        this.add(value, desc, false);
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
            return new HelpSection(title, PADDING_2)
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
        return new HelpSingleValue(title);
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
