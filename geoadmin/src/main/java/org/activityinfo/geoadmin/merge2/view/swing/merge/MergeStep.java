package org.activityinfo.geoadmin.merge2.view.swing.merge;

import org.activityinfo.geoadmin.merge2.model.ImportModel;
import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.swing.Step;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;

public class MergeStep implements Step {


    private final ImportView viewModel;

    public MergeStep(ImportView viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public String getLabel() {
        return "Match Rows";
    }

    @Override
    public StepPanel createView() {
        return new MergePanel(viewModel);
    }
}
