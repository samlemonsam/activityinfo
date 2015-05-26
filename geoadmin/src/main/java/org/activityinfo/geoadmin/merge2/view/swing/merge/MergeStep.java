package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.view.swing.Step;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;

public class MergeStep implements Step {


    private final ImportModel store;

    public MergeStep(ImportModel store) {
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
