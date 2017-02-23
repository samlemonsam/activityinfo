package org.activityinfo.geoadmin.merge2.view.swing;

import javax.swing.*;


public interface Step {
    String getLabel();
    StepPanel createView();
}
