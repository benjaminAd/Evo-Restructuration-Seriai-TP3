package com.exemple.tp2.library;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
    List<String> classes = new ArrayList<>();
    Pair<Cluster, Cluster> pairs = new Pair<>();
    private static int i = 0;

    public Cluster(String name) {
        classes.add(name);
    }

    public Cluster(Cluster c1, Cluster c2) {
        pairs.setKey(c1);
        pairs.setValue(c2);
    }

    public List<String> getClasses() {
        return classes;
    }

    @Override
    public String toString() {
        if (!classes.isEmpty() && pairs.getKey() == null) {
            StringBuilder st = new StringBuilder();
            for (int i = 0; i < classes.size(); i++) {
                if (i == 0) st.append("(");
                st.append(classes.get(i));
                if (i != classes.size() - 1) st.append(",");
                if (i == classes.size() - 1) st.append(")");
            }
            return st.toString();
        }
        i++;
        return "(" + i + " " + pairs.getKey() + "-->" + pairs.getValue() + " " + i + ")";
    }
}
