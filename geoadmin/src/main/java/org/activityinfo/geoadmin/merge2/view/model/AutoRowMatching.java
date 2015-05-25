package org.activityinfo.geoadmin.merge2.view.model;


public class AutoRowMatching {
    private final FormMapping formMapping;
    private final int[] assignment;

    public AutoRowMatching(FormMapping formMapping, int[] assignment) {

        this.formMapping = formMapping;
        this.assignment = assignment;
    }

    public FormMapping getFormMapping() {
        return formMapping;
    }

    public int getBestSourceMatchForTarget(int targetRowIndex) {
        return assignment[targetRowIndex];
    }
}
