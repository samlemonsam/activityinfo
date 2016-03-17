package org.activityinfo.geoadmin.merge2.view.swing.match.select;

import javax.swing.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class SyncingAdjustmentListener implements AdjustmentListener {
    
    private final JScrollBar scrollBar;

    public SyncingAdjustmentListener(JScrollBar scrollBar) {
        this.scrollBar = scrollBar;
    }

    @Override
    public void adjustmentValueChanged(AdjustmentEvent e) {
        scrollBar.setValue(e.getValue());
    }
}
