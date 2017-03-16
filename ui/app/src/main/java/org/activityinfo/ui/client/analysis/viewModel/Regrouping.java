package org.activityinfo.ui.client.analysis.viewModel;

import java.util.List;

public class Regrouping {
    private int[] groupArray;
    private List<String[]> groups;

    public Regrouping(int[] groupArray, List<String[]> groups) {
        this.groupArray = groupArray;
        this.groups = groups;
    }

    public int[] getGroupArray() {
        return groupArray;
    }

    public List<String[]> getGroups() {
        return groups;
    }
}
