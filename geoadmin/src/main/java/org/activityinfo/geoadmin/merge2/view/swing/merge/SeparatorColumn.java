package org.activityinfo.geoadmin.merge2.view.swing.merge;

import javax.swing.*;
import java.awt.*;

/**
 * Provides a visual separation between the source and target columns
 */
public class SeparatorColumn extends MergeTableColumn {
    @Override
    public String getHeader() {
        return "";
    }

    @Override
    public String getValue(int rowIndex) {
        return null;
    }

    @Override
    public Color getColor(int rowIndex) {
        return UIManager.getColor("Panel.background");
    }

    @Override
    public int getTextAlignment() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 15;
    }

    @Override
    public boolean isResizable() {
        return false;
    }
}
