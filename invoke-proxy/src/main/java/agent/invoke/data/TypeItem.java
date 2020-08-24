package agent.invoke.data;

public class TypeItem {
    private final String className;
    private final boolean array;
    private final int dimensions;

    public TypeItem(String className) {
        this(className, false, -1);
    }

    public TypeItem(String className, boolean array, int dimensions) {
        this.className = className;
        this.array = array;
        this.dimensions = dimensions;
    }

    public String getClassName() {
        return className;
    }

    public boolean isArray() {
        return array;
    }

    public int getDimensions() {
        return dimensions;
    }
}
