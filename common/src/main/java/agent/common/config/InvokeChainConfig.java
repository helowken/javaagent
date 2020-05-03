package agent.common.config;

import java.util.Objects;

import static agent.base.utils.AssertUtils.assertTrue;

public class InvokeChainConfig extends AbstractAgentConfig {
    private ClassFilterConfig matchClassFilter;
    private MethodFilterConfig matchMethodFilter;
    private ConstructorFilterConfig matchConstructorFilter;
    private ClassFilterConfig searchClassFilter;
    private MethodFilterConfig searchMethodFilter;
    private ConstructorFilterConfig searchConstructorFilter;
    private int maxLevel = 100;

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
        assertTrue(maxLevel >= 1, "Max level must >= 1");
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
}
