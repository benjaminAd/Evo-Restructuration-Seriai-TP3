package com.exemple.tp2.library;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Cluster {
    List<String> classes = new ArrayList<>();

    public Cluster(String name) {
        classes.add(name);
    }

    public Cluster(Cluster c1, Cluster c2) {
        classes.addAll(c1.classes);
        classes.addAll(c2.classes);
    }

    public List<String> getClasses() {
        return classes;
    }
}
