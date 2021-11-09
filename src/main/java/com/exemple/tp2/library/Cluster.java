package com.exemple.tp2.library;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class Cluster {
    private List<String> classes = new ArrayList<>();
    private Pair<Cluster, Cluster> pairs = new Pair<>();
    private float value;
    private static int i = 0;

    public Cluster(String name) {
        this.classes.add(name);
    }

    public Cluster(Cluster c1, Cluster c2, float value) {
        pairs.setKey(c1);
        pairs.setValue(c2);
        this.value = value;
    }

    public List<String> getClasses() {
        if (!classes.isEmpty())
            return classes;

        List<String> classNames = new ArrayList<>(pairs.getKey().getClasses());
        classNames.addAll(pairs.getValue().getClasses());

        return classNames;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##############");
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
        return "(" + i + " " + "( " + pairs.getKey() + "-->" + pairs.getValue() + " " + df.format(this.value) + " )" + " " + i + ")";
    }
}
