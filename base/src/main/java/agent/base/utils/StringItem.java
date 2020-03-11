package agent.base.utils;

public class StringItem {
    private final String str;

    public StringItem(String str) {
        this.str = str;
    }

    @Override
    public String toString() {
        return str;
    }

    public StringItem replaceAll(String old, String newStr) {
        StringBuilder sb = new StringBuilder();
        int start = 0;
        int len = str.length();
        int oldLen = old.length();
        while (true) {
            int pos = str.indexOf(old, start);
            if (pos > -1) {
                sb.append(str, start, pos);
                sb.append(newStr);
                start = pos + oldLen;
                if (start >= len)
                    break;
            } else {
                sb.append(
                        str.substring(start)
                );
                break;
            }
        }
        return new StringItem(
                sb.toString()
        );
    }
}
