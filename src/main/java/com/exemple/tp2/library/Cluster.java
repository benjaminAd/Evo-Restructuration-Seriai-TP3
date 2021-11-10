package com.exemple.tp2.library;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
    private List<String> classes = new ArrayList<>();
    private Pair<Cluster, Cluster> pairs = new Pair<>();
    private float value;
    private int compteur;

    public Cluster(String name) {
        this.classes.add(name);
    }

    public Cluster(Cluster c1, Cluster c2, float value, int compteur) {
        pairs.setKey(c1);
        pairs.setValue(c2);
        this.value = value;
        this.compteur = compteur;
    }

    public float getValue() {
        return value;
    }

    public List<String> getClasses() {
        if (!classes.isEmpty())
            return classes;

        List<String> classNames = new ArrayList<>(pairs.getKey().getClasses());
        classNames.addAll(pairs.getValue().getClasses());

        return classNames;
    }

    public Pair<Cluster, Cluster> getPairs() {
        return pairs;
    }

    @Override
    public String toString() {
        if (!classes.isEmpty() && pairs.getKey() == null) {
            StringBuilder st = new StringBuilder();
            for (int i = 0; i < classes.size(); i++) {
                if (i == 0) st.append("(");
                st.append(classes.get(i));
                if (i != classes.size() - 1) st.append(";");
                if (i == classes.size() - 1) st.append(")");
            }
            return st.toString();
        }
        return "(" + compteur + " " + pairs.getKey() + "--" + pairs.getValue() + " " + compteur + ")";
    }
}
