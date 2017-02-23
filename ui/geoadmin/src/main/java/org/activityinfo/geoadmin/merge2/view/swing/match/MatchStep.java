package org.activityinfo.geoadmin.merge2.view.swing.match;

import org.activityinfo.geoadmin.merge2.view.ImportView;
import org.activityinfo.geoadmin.merge2.view.swing.Step;
import org.activityinfo.geoadmin.merge2.view.swing.StepPanel;

public class MatchStep implements Step {


    private final ImportView viewModel;

    public MatchStep(ImportView viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public String getLabel() {
        return "Match Rows";
    }

    @Override
    public StepPanel createView() {
        return new MatchStepPanel(viewModel);
    }
}
