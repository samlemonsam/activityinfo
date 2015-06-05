package org.activityinfo.geoadmin.merge2.view.swing.match;

import org.activityinfo.geoadmin.merge2.view.ImportView;

import javax.swing.*;


public class SelectDialog extends JDialog {
    

    public SelectDialog(MatchStepPanel parent, ImportView viewModel, int matchRowIndex) {
        super(SwingUtilities.getWindowAncestor(parent), "Choose", ModalityType.APPLICATION_MODAL);

    }
}
