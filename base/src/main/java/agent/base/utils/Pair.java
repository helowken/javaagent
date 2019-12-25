package agent.base.utils;

public class Pair<L, R> {
    public final L left;
    public final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }

    public void exec(ExecFunc<L, R> func) {
        func.exec(this.left, this.right);
    }

    public <T> T exec(ExecValueFunc<L, R, T> func) {
        return func.exec(this.left, this.right);
    }

    public interface ExecFunc<L, R> {
        void exec(L left, R right);
    }

    public interface ExecValueFunc<L, R, T> {
        T exec(L left, R right);
    }

    @Override
    public String toString() {
        return "{left: " + left + ", right: " + right + "}";
    }
}
