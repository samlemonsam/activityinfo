package org.activityinfo.model;

import org.activityinfo.json.AutoJson;

@AutoJson
public class DummyModel {

    int age;
    String name;
    boolean married;
    double score;

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public boolean isMarried() {
        return married;
    }

    public double getScore() {
        return score;
    }
}
