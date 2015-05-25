package org.activityinfo.geoadmin.merge2.view.swing.merge;

import java.awt.*;

/**
 * Single column in the merge table
 */
public interface MergeTableColumn {

    String getHeader();

    String getValue(int rowIndex);

    Color getColor(int rowIndex);

    int getTextAlignment();

    int getWidth();

    boolean isResizable();

}
