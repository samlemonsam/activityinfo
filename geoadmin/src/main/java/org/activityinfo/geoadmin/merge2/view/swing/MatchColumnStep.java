package org.activityinfo.geoadmin.merge2.view.swing;

import org.activityinfo.geoadmin.merge2.view.ImportView;


public class MatchColumnStep implements Step {

    private final ImportView viewModel;

    public MatchColumnStep(ImportView viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public String getLabel() {
        return "Match Columns";
    }
    
    @Override
    public MatchColumnPanel createView() {
        return new MatchColumnPanel(viewModel);
    }
}
