package com.exemple.tp2.library;

public class Triplet<X, Y, Z> {
    private X first;
    private Y second;
    private Z third;

    public Triplet() {
        this.first = null;
        this.second = null;
        this.third = null;
    }

    public Triplet(X first, Y second, Z third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Triplet(Pair<X, Y> pair, Z third) {
        this.first = pair.getKey();
        this.second = pair.getValue();
        this.third = third;
    }

    public X getFirst() {
        return first;
    }

    public Y getSecond() {
        return second;
    }

    public Z getThird() {
        return third;
    }
}
