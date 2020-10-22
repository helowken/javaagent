package agent.common.config;

import agent.common.utils.annotation.PojoProperty;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InvokeChainConfig that = (InvokeChainConfig) o;
        return maxLevel == that.maxLevel &&
                Objects.equals(matchClassFilter, that.matchClassFilter) &&
                Objects.equals(matchMethodFilter, that.matchMethodFilter) &&
                Objects.equals(matchConstructorFilter, that.matchConstructorFilter) &&
                Objects.equals(searchClassFilter, that.searchClassFilter) &&
                Objects.equals(searchMethodFilter, that.searchMethodFilter) &&
                Objects.equals(searchConstructorFilter, that.searchConstructorFilter);
    }

    @Override
    public int hashCode() {

        return Objects.hash(matchClassFilter, matchMethodFilter, matchConstructorFilter, searchClassFilter, searchMethodFilter, searchConstructorFilter, maxLevel);
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
                '}';
    }

    public static InvokeChainConfig matchAll() {
        ConstructorFilterConfig constructorFilterConfig = new ConstructorFilterConfig();
        constructorFilterConfig.setIncludes(Collections.singleton("*"));

        MethodFilterConfig methodFilterConfig = new MethodFilterConfig();
        methodFilterConfig.setIncludes(Collections.singleton("*"));

        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        invokeChainConfig.setMatchConstructorFilter(constructorFilterConfig);
        invokeChainConfig.setMatchMethodFilter(methodFilterConfig);
        return invokeChainConfig;
    }

    public static InvokeChainConfig matchAllMethods() {
        MethodFilterConfig methodFilterConfig = new MethodFilterConfig();
        methodFilterConfig.setIncludes(Collections.singleton("*"));

        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        invokeChainConfig.setMatchMethodFilter(methodFilterConfig);
        return invokeChainConfig;
    }

    public static InvokeChainConfig matchAllConstructors() {
        ConstructorFilterConfig constructorFilterConfig = new ConstructorFilterConfig();
        constructorFilterConfig.setIncludes(Collections.singleton("*"));

        InvokeChainConfig invokeChainConfig = new InvokeChainConfig();
        invokeChainConfig.setMatchConstructorFilter(constructorFilterConfig);
        return invokeChainConfig;
    }
}
