package agent.common.args.parse;

public class FilterItem {
    final String classStr;
    final String methodStr;
    final String constructorStr;
    final String searchClassStr;
    final String searchMethodStr;
    final String searchConstructorStr;
    final int searchLevel;
    final int searchClassMaxSize;
    final String chainClassStr;
    final String chainMethodStr;
    final String chainConstructorStr;
    final int chainClassMaxSize;

    public FilterItem(String classStr, String methodStr, String constructorStr) {
        this(classStr, methodStr, constructorStr, null, null, null, -1, -1, null, null, null, -1);
    }

    public FilterItem(String classStr, String methodStr, String constructorStr, String searchClassStr, String searchMethodStr,
                      String searchConstructorStr, int searchLevel, int searchClassMaxSize,
                      String chainClassStr, String chainMethodStr, String chainConstructorStr, int chainClassMaxSize) {
        this.classStr = classStr;
        this.methodStr = methodStr;
        this.constructorStr = constructorStr;
        this.searchClassStr = searchClassStr;
        this.searchMethodStr = searchMethodStr;
        this.searchConstructorStr = searchConstructorStr;
        this.searchLevel = searchLevel;
        this.searchClassMaxSize = searchClassMaxSize;
        this.chainClassStr = chainClassStr;
        this.chainMethodStr = chainMethodStr;
        this.chainConstructorStr = chainConstructorStr;
        this.chainClassMaxSize = chainClassMaxSize;
    }
}
