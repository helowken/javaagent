package agent.dynamic.attach;

class JarPathAndOption {
    final String jarPath;
    final String option;

    JarPathAndOption(String jarPath, String option) {
        this.jarPath = jarPath;
        this.option = option;
    }

    @Override
    public String toString() {
        return "jarPath='" + jarPath + '\'' +
                ", option='" + option + '\'';
    }
}
