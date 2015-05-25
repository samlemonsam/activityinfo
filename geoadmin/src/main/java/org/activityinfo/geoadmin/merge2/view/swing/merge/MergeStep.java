package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.MergeModelStore;
import org.activityinfo.geoadmin.merge2.view.swing.Step;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;

import javax.swing.*;

public class MergeStep implements Step {


    private final MergeModelStore store;

    public MergeStep(MergeModelStore store) {
        this.store = store;
    }

    @Override
    public String getLabel() {
        return "Match Rows";
    }

    @Override
    public StepPanel createView() {
        return new MergePanel(store);
    }
}
