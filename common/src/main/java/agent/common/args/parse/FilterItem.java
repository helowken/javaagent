package agent.common.args.parse;

class FilterItem {
    final String classStr;
    final String methodStr;
    final String constructorStr;
    final String searchClassStr;
    final String searchMethodStr;
    final String searchConstructorStr;
    final String chainClassStr;
    final String chainMethodStr;
    final String chainConstructorStr;
    final int searchLevel;

    FilterItem(String classStr, String methodStr, String constructorStr,
               String searchClassStr, String searchMethodStr, String searchConstructorStr, int searchLevel,
               String chainClassStr, String chainMethodStr, String chainConstructorStr) {
        this.classStr = classStr;
        this.methodStr = methodStr;
        this.constructorStr = constructorStr;
        this.searchClassStr = searchClassStr;
        this.searchMethodStr = searchMethodStr;
        this.searchConstructorStr = searchConstructorStr;
        this.searchLevel = searchLevel;
        this.chainClassStr = chainClassStr;
        this.chainMethodStr = chainMethodStr;
        this.chainConstructorStr = chainConstructorStr;
    }
}
