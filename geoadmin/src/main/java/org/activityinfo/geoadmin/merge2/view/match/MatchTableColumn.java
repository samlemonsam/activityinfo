package org.activityinfo.geoadmin.merge2.view.match;

import javax.swing.*;
import java.awt.*;

/**
 * Single column in the merge table
 */
public abstract class MatchTableColumn {

    public abstract String getHeader();

    public abstract String getValue(int rowIndex);

    public Color getColor(int rowIndex) {
        return Color.WHITE;
    }

    public int getTextAlignment() {
        return SwingConstants.LEFT;
    }

    public int getWidth() {
        return -1;
    }
    
    public boolean isResizable() {
        return true;
    }

}
