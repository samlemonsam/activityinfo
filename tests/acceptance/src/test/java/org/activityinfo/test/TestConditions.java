package org.activityinfo.test;

import com.google.inject.Module;

import java.util.ArrayList;
import java.util.List;


public class TestConditions {
    private String id;
    private List<Module> modules = new ArrayList<>();

    public TestConditions(String id, List<Module> modules) {
        this.id = id;
        this.modules = modules;
    }

    public String getId() {
        return id;
    }

    public List<Module> getModules() {
        return modules;
    }
}
