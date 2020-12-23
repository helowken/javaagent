package agent.common.config;

import agent.common.struct.impl.annotation.PojoProperty;

import java.util.Collections;
import java.util.Objects;

public class InvokeChainConfig extends AbstractValidConfig {
    @PojoProperty(index = 0)
    private ClassFilterConfig matchClassFilter;
    @PojoProperty(index = 1)
    private MethodFilterConfig matchMethodFilter;
    @PojoProperty(index = 2)
    private ConstructorFilterConfig matchConstructorFilter;
    @PojoProperty(index = 3)
    private ClassFilterConfig searchClassFilter;
    @PojoProperty(index = 4)
    private MethodFilterConfig searchMethodFilter;
    @PojoProperty(index = 5)
    private ConstructorFilterConfig searchConstructorFilter;
    @PojoProperty(index = 6)
    private int maxLevel = -1;
    @PojoProperty(index = 7)
    private int searchMaxSize = -1;
    @PojoProperty(index = 8)
    private int matchMaxSize = -1;

    public boolean isEmpty() {
        return getMatchClassFilter() == null &&
                getMatchMethodFilter() == null &&
                getMatchConstructorFilter() == null &&
                getSearchClassFilter() == null &&
                getSearchMethodFilter() == null &&
                getSearchConstructorFilter() == null;
    }

    public ClassFilterConfig getSearchClassFilter() {
        return searchClassFilter;
    }

    public void setSearchClassFilter(ClassFilterConfig searchClassFilter) {
        this.searchClassFilter = searchClassFilter;
    }

    public MethodFilterConfig getSearchMethodFilter() {
        return searchMethodFilter;
    }

    public void setSearchMethodFilter(MethodFilterConfig searchMethodFilter) {
        this.searchMethodFilter = searchMethodFilter;
    }

    public ConstructorFilterConfig getSearchConstructorFilter() {
        return searchConstructorFilter;
    }

    public void setSearchConstructorFilter(ConstructorFilterConfig searchConstructorFilter) {
        this.searchConstructorFilter = searchConstructorFilter;
    }

    @Override
    public void validate() {
        validateIfNotNull(
                matchClassFilter,
                matchMethodFilter,
                matchConstructorFilter,
                searchClassFilter,
                searchMethodFilter,
                searchConstructorFilter
        );
    }

    public ClassFilterConfig getMatchClassFilter() {
        return matchClassFilter;
    }

    public void setMatchClassFilter(ClassFilterConfig matchClassFilter) {
        this.matchClassFilter = matchClassFilter;
    }

    public MethodFilterConfig getMatchMethodFilter() {
        return matchMethodFilter;
    }

    public void setMatchMethodFilter(MethodFilterConfig matchMethodFilter) {
        this.matchMethodFilter = matchMethodFilter;
    }

    public ConstructorFilterConfig getMatchConstructorFilter() {
        return matchConstructorFilter;
    }

    public void setMatchConstructorFilter(ConstructorFilterConfig matchConstructorFilter) {
        this.matchConstructorFilter = matchConstructorFilter;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public int getSearchMaxSize() {
        return searchMaxSize;
    }

    public void setSearchMaxSize(int searchMaxSize) {
        this.searchMaxSize = searchMaxSize;
    }

    public int getMatchMaxSize() {
        return matchMaxSize;
    }

    public void setMatchMaxSize(int matchMaxSize) {
        this.matchMaxSize = matchMaxSize;
    }

    public static InvokeChainConfig matchAll(String searchClassStr, String matchClassStr) {
        ConstructorFilterConfig constructorFilterConfig = new ConstructorFilterConfig();
        constructorFilterConfig.setIncludes(Collections.singleton("*"));

        MethodFilterConfig methodFilterConfig = new MethodFilterConfig();
        methodFilterConfig.setIncludes(Collections.singleton("*"));

        ClassFilterConfig searchClassFilter = new ClassFilterConfig();
        searchClassFilter.setIncludes(Collections.singleton(searchClassStr));

        ClassFilterConfig matchClassFilter = new ClassFilterConfig();
        matchClassFilter.setIncludes(Collections.singleton(matchClassStr));

        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        invokeChainConfig.setSearchClassFilter(searchClassFilter);
        invokeChainConfig.setSearchConstructorFilter(constructorFilterConfig);
        invokeChainConfig.setSearchMethodFilter(methodFilterConfig);
        invokeChainConfig.setMatchClassFilter(matchClassFilter);
        invokeChainConfig.setMatchConstructorFilter(constructorFilterConfig);
        invokeChainConfig.setMatchMethodFilter(methodFilterConfig);
        return invokeChainConfig;
    }

    public static InvokeChainConfig matchAllMethods(String searchClassStr, String matchClassStr) {
        ClassFilterConfig searchClassFilter = new ClassFilterConfig();
        searchClassFilter.setIncludes(Collections.singleton(searchClassStr));

        ClassFilterConfig matchClassFilter = new ClassFilterConfig();
        matchClassFilter.setIncludes(Collections.singleton(matchClassStr));

        MethodFilterConfig methodFilterConfig = new MethodFilterConfig();
        methodFilterConfig.setIncludes(Collections.singleton("*"));

        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        invokeChainConfig.setSearchClassFilter(searchClassFilter);
        invokeChainConfig.setMatchClassFilter(matchClassFilter);
        invokeChainConfig.setMatchMethodFilter(methodFilterConfig);
        return invokeChainConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeChainConfig that = (InvokeChainConfig) o;
        return maxLevel == that.maxLevel &&
                searchMaxSize == that.searchMaxSize &&
                matchMaxSize == that.matchMaxSize &&
                Objects.equals(matchClassFilter, that.matchClassFilter) &&
                Objects.equals(matchMethodFilter, that.matchMethodFilter) &&
                Objects.equals(matchConstructorFilter, that.matchConstructorFilter) &&
                Objects.equals(searchClassFilter, that.searchClassFilter) &&
                Objects.equals(searchMethodFilter, that.searchMethodFilter) &&
                Objects.equals(searchConstructorFilter, that.searchConstructorFilter);
    }

    @Override
    public int hashCode() {

        return Objects.hash(matchClassFilter, matchMethodFilter, matchConstructorFilter, searchClassFilter, searchMethodFilter, searchConstructorFilter, maxLevel, searchMaxSize, matchMaxSize);
    }

    @Override
    public String toString() {
        return "InvokeChainConfig{" +
                "matchClassFilter=" + matchClassFilter +
                ", matchMethodFilter=" + matchMethodFilter +
                ", matchConstructorFilter=" + matchConstructorFilter +
                ", searchClassFilter=" + searchClassFilter +
                ", searchMethodFilter=" + searchMethodFilter +
                ", searchConstructorFilter=" + searchConstructorFilter +
                ", maxLevel=" + maxLevel +
                ", searchMaxSize=" + searchMaxSize +
                ", matchMaxSize=" + matchMaxSize +
                '}';
    }
}
