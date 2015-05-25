package org.activityinfo.geoadmin.merge2.view.swing.merge;

import java.awt.*;

/**
 * Displays the resolution status of the row
 */
public class ResolutionColumn implements MergeTableColumn {
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
        return null;
    }

    @Override
    public int getTextAlignment() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public boolean isResizable() {
        return false;
    }
}
