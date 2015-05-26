package org.activityinfo.geoadmin.merge2.view.swing;

import org.activityinfo.geoadmin.merge2.model.ImportModel;


public class MatchColumnStep implements Step {

    private final ImportModel store;

    public MatchColumnStep(ImportModel store) {
        this.store = store;
    }

    @Override
    public String getLabel() {
        return "Match Columns";
    }
    
    @Override
    public MatchColumnPanel createView() {
        return new MatchColumnPanel(store);
    }
}
