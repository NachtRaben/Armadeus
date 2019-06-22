package com.nachtraben.core.util.eval;

public class EvalResult<L,M,R> {

    private L left;
    private M middle;
    private R right;

    public EvalResult(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public M getMiddle() {
        return middle;
    }

    public R getRight() {
        return right;
    }
}
