package org.activityinfo.geoadmin.merge2.view.swing;

import org.activityinfo.geoadmin.merge2.MergeModelStore;


public class MatchColumnStep implements Step {

    private final MergeModelStore store;

    public MatchColumnStep(MergeModelStore store) {
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
